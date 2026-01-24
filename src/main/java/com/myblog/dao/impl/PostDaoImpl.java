package com.myblog.dao.impl;

import com.myblog.dao.PostDao;
import com.myblog.dao.TagDao;
import com.myblog.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PostDaoImpl implements PostDao {

    private static final Logger log = LoggerFactory.getLogger(PostDaoImpl.class);
    private final JdbcTemplate jdbcTemplate;
    private final TagDao tagDao;

    public PostDaoImpl(JdbcTemplate jdbcTemplate, TagDao tagDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.tagDao = tagDao;
    }

    @Override
    public Post create(Post post) {
        String sql = "INSERT INTO posts (title, text, likes_count) VALUES (?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            return ps;
        }, keyHolder);

        Long postId = keyHolder.getKey().longValue();
        post.setId(postId);
        post.setLikesCount(0);
        post.setCommentsCount(0);

        // Сохранить теги
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            saveTags(postId, post.getTags());
        }

        return findById(postId).orElse(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        String sql = "SELECT p.id, p.title, p.text, p.likes_count, p.created_at, p.updated_at, " +
                     "(SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) as comments_count " +
                     "FROM posts p WHERE p.id = ?";
        
        try {
            Post post = jdbcTemplate.queryForObject(sql, new PostRowMapper(), id);
            if (post != null) {
                post.setTags(tagDao.findByPostId(id).stream()
                    .map(tag -> tag.getName())
                    .toList());
            }
            return Optional.ofNullable(post);
        } catch (Exception e) {
            log.debug("Post not found with id: {}", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Post> findAll(String search, int pageNumber, int pageSize) {
        List<String> tags = new ArrayList<>();
        String titleSearch = parseSearchQuery(search, tags);

        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.title, p.text, p.likes_count, p.created_at, p.updated_at, " +
            "(SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) as comments_count " +
            "FROM posts p WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        // Фильтр по подстроке в названии
        if (titleSearch != null && !titleSearch.isEmpty()) {
            sql.append(" AND LOWER(p.title) LIKE LOWER(?)");
            params.add("%" + titleSearch + "%");
        }

        // Фильтр по тегам
        if (!tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                sql.append(" AND EXISTS (SELECT 1 FROM post_tags pt " +
                          "JOIN tags t ON pt.tag_id = t.id " +
                          "WHERE pt.post_id = p.id AND t.name = ?)");
                params.add(tags.get(i));
            }
        }

        sql.append(" ORDER BY p.created_at DESC");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNumber - 1) * pageSize);

        List<Post> posts = jdbcTemplate.query(sql.toString(), new PostRowMapper(), params.toArray());

        // Загрузить теги для каждого поста
        for (Post post : posts) {
            post.setTags(tagDao.findByPostId(post.getId()).stream()
                .map(tag -> tag.getName())
                .toList());
            
            // Обрезать текст до 128 символов для списка
            if (post.getText().length() > 128) {
                post.setText(post.getText().substring(0, 128) + "…");
            }
        }

        return posts;
    }

    @Override
    public Post update(Post post) {
        String sql = "UPDATE posts SET title = ?, text = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, post.getTitle(), post.getText(), post.getId());

        // Обновить теги
        tagDao.unlinkAllTagsFromPost(post.getId());
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            saveTags(post.getId(), post.getTags());
        }

        return findById(post.getId()).orElse(post);
    }

    @Override
    public void delete(Long id) {
        // Удалить все комментарии
        String deleteComments = "DELETE FROM comments WHERE post_id = ?";
        jdbcTemplate.update(deleteComments, id);

        // Удалить все связи с тегами
        String deletePostTags = "DELETE FROM post_tags WHERE post_id = ?";
        jdbcTemplate.update(deletePostTags, id);

        // Удалить изображение
        String deleteImage = "DELETE FROM post_images WHERE post_id = ?";
        jdbcTemplate.update(deleteImage, id);

        // Удалить сам пост
        String deletePost = "DELETE FROM posts WHERE id = ?";
        jdbcTemplate.update(deletePost, id);
    }

    @Override
    public void incrementLikes(Long id) {
        String sql = "UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void decrementLikes(Long id) {
        String sql = "UPDATE posts SET likes_count = likes_count - 1 WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public int getTotalCount(String search) {
        List<String> tags = new ArrayList<>();
        String titleSearch = parseSearchQuery(search, tags);

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM posts p WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (titleSearch != null && !titleSearch.isEmpty()) {
            sql.append(" AND LOWER(p.title) LIKE LOWER(?)");
            params.add("%" + titleSearch + "%");
        }

        if (!tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                sql.append(" AND EXISTS (SELECT 1 FROM post_tags pt " +
                          "JOIN tags t ON pt.tag_id = t.id " +
                          "WHERE pt.post_id = p.id AND t.name = ?)");
                params.add(tags.get(i));
            }
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    @Override
    public void saveImage(Long postId, byte[] imageData, String contentType) {
        String deleteSql = "DELETE FROM post_images WHERE post_id = ?";
        jdbcTemplate.update(deleteSql, postId);

        String insertSql = "INSERT INTO post_images (post_id, image_data, content_type) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, postId, imageData, contentType);
    }

    @Override
    public Optional<byte[]> getImage(Long postId) {
        String sql = "SELECT image_data FROM post_images WHERE post_id = ?";
        try {
            byte[] imageData = jdbcTemplate.queryForObject(sql, byte[].class, postId);
            return Optional.ofNullable(imageData);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getImageContentType(Long postId) {
        String sql = "SELECT content_type FROM post_images WHERE post_id = ?";
        try {
            String contentType = jdbcTemplate.queryForObject(sql, String.class, postId);
            return Optional.ofNullable(contentType);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void saveTags(Long postId, List<String> tagNames) {
        for (String tagName : tagNames) {
            if (tagName == null || tagName.trim().isEmpty()) {
                continue;
            }
            
            // Удалить # если есть
            String cleanTagName = tagName.startsWith("#") ? tagName.substring(1) : tagName;
            
            // Найти или создать тег
            Optional<com.myblog.model.Tag> existingTag = tagDao.findByName(cleanTagName);
            Long tagId;
            if (existingTag.isPresent()) {
                tagId = existingTag.get().getId();
            } else {
                com.myblog.model.Tag newTag = tagDao.create(cleanTagName);
                tagId = newTag.getId();
            }
            
            // Связать тег с постом
            tagDao.linkTagToPost(tagId, postId);
        }
    }

    private String parseSearchQuery(String search, List<String> tags) {
        if (search == null || search.trim().isEmpty()) {
            return "";
        }

        String[] words = search.trim().split("\\s+");
        StringBuilder titleSearch = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            
            if (word.startsWith("#")) {
                // Это тег
                String tagName = word.substring(1);
                if (!tagName.isEmpty()) {
                    tags.add(tagName);
                }
            } else {
                // Это часть поиска по названию
                if (titleSearch.length() > 0) {
                    titleSearch.append(" ");
                }
                titleSearch.append(word);
            }
        }

        return titleSearch.toString();
    }

    private static class PostRowMapper implements RowMapper<Post> {
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            Post post = new Post();
            post.setId(rs.getLong("id"));
            post.setTitle(rs.getString("title"));
            post.setText(rs.getString("text"));
            post.setLikesCount(rs.getInt("likes_count"));
            post.setCommentsCount(rs.getInt("comments_count"));
            post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return post;
        }
    }
}

