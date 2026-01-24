package com.myblog.service.impl;

import com.myblog.dao.PostDao;
import com.myblog.dto.CreatePostRequest;
import com.myblog.dto.PostListResponse;
import com.myblog.dto.UpdatePostRequest;
import com.myblog.model.Post;
import com.myblog.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);
    private final PostDao postDao;

    public PostServiceImpl(PostDao postDao) {
        this.postDao = postDao;
    }

    @Override
    @Transactional(readOnly = true)
    public PostListResponse getPosts(String search, int pageNumber, int pageSize) {
        log.debug("Getting posts with search: {}, page: {}, size: {}", search, pageNumber, pageSize);
        
        List<Post> posts = postDao.findAll(search, pageNumber, pageSize);
        int totalCount = postDao.getTotalCount(search);
        int lastPage = (int) Math.ceil((double) totalCount / pageSize);
        
        return new PostListResponse(posts, pageNumber > 1, pageNumber < lastPage, lastPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Post> getPostById(Long id) {
        log.debug("Getting post by id: {}", id);
        return postDao.findById(id);
    }

    @Override
    @Transactional
    public Post createPost(CreatePostRequest request) {
        log.debug("Creating new post with title: {}", request.getTitle());
        
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        post.setTags(request.getTags());
        
        return postDao.create(post);
    }

    @Override
    @Transactional
    public Post updatePost(Long id, UpdatePostRequest request) {
        log.debug("Updating post with id: {}", id);
        
        Optional<Post> existingPost = postDao.findById(id);
        if (existingPost.isEmpty()) {
            throw new IllegalArgumentException("Post not found with id: " + id);
        }
        
        Post post = existingPost.get();
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        post.setTags(request.getTags());
        
        return postDao.update(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        log.debug("Deleting post with id: {}", id);

        Optional<Post> existingPost = postDao.findById(id);
        if (existingPost.isEmpty()) {
            throw new IllegalArgumentException("Post not found with id: " + id);
        }

        postDao.delete(id);
    }

    @Override
    @Transactional
    public int incrementLikes(Long id) {
        log.debug("Incrementing likes for post with id: {}", id);

        Optional<Post> existingPost = postDao.findById(id);
        if (existingPost.isEmpty()) {
            throw new IllegalArgumentException("Post not found with id: " + id);
        }

        postDao.incrementLikes(id);

        return existingPost.get().getLikesCount() + 1;
    }

    @Override
    @Transactional
    public int decrementLikes(Long id) {
        log.debug("Decrementing likes for post with id: {}", id);

        Optional<Post> existingPost = postDao.findById(id);
        if (existingPost.isEmpty()) {
            throw new IllegalArgumentException("Post not found with id: " + id);
        }

        int currentLikes = existingPost.get().getLikesCount();
        if (currentLikes > 0) {
            postDao.decrementLikes(id);
            return currentLikes - 1;
        }

        return 0;
    }

    @Override
    @Transactional
    public void saveImage(Long postId, byte[] imageData, String contentType) {
        log.debug("Saving image for post with id: {}", postId);
        postDao.saveImage(postId, imageData, contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<byte[]> getImage(Long postId) {
        log.debug("Getting image for post with id: {}", postId);
        return postDao.getImage(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getImageContentType(Long postId) {
        return postDao.getImageContentType(postId);
    }
}

