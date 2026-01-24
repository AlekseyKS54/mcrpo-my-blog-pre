package com.myblog.dao.impl;

import com.myblog.dao.CommentDao;
import com.myblog.model.Comment;
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
import java.util.List;
import java.util.Optional;

@Repository
public class CommentDaoImpl implements CommentDao {

    private static final Logger log = LoggerFactory.getLogger(CommentDaoImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public CommentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Comment create(Comment comment) {
        String sql = "INSERT INTO comments (text, post_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            return ps;
        }, keyHolder);

        Long commentId = keyHolder.getKey().longValue();
        comment.setId(commentId);

        return findById(commentId).orElse(comment);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        String sql = "SELECT id, text, post_id, created_at, updated_at FROM comments WHERE id = ?";
        try {
            Comment comment = jdbcTemplate.queryForObject(sql, new CommentRowMapper(), id);
            return Optional.ofNullable(comment);
        } catch (Exception e) {
            log.debug("Comment not found with id: {}", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        String sql = "SELECT id, text, post_id, created_at, updated_at FROM comments WHERE post_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, new CommentRowMapper(), postId);
    }

    @Override
    public Comment update(Comment comment) {
        String sql = "UPDATE comments SET text = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, comment.getText(), comment.getId());
        return findById(comment.getId()).orElse(comment);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM comments WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public int countByPostId(Long postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId);
        return count != null ? count : 0;
    }

    private static class CommentRowMapper implements RowMapper<Comment> {
        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Comment comment = new Comment();
            comment.setId(rs.getLong("id"));
            comment.setText(rs.getString("text"));
            comment.setPostId(rs.getLong("post_id"));
            comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            comment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return comment;
        }
    }
}

