package com.myblog.service;

import com.myblog.dto.CreatePostRequest;
import com.myblog.dto.PostListResponse;
import com.myblog.dto.UpdatePostRequest;
import com.myblog.model.Post;
import com.myblog.repository.PostRepository;
import com.myblog.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setText("Test content");
        testPost.setTags(Arrays.asList("tag1", "tag2"));
        testPost.setLikesCount(0);
        testPost.setCommentsCount(0);
    }

    @Test
    void testGetPosts() {
        // Given
        List<Post> posts = Arrays.asList(testPost);
        when(postRepository.findAll(anyString(), anyInt(), anyInt())).thenReturn(posts);
        when(postRepository.getTotalCount(anyString())).thenReturn(1);

        // When
        PostListResponse response = postService.getPosts("", 1, 10);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getPosts().size());
        assertEquals(testPost.getId(), response.getPosts().get(0).getId());
        assertFalse(response.isHasPrev());
        assertFalse(response.isHasNext());
        assertEquals(1, response.getLastPage());
        
        verify(postRepository).findAll("", 1, 10);
        verify(postRepository).getTotalCount("");
    }

    @Test
    void testGetPostById() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // When
        Optional<Post> result = postService.getPostById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPost.getId(), result.get().getId());
        assertEquals(testPost.getTitle(), result.get().getTitle());
        
        verify(postRepository).findById(1L);
    }

    @Test
    void testCreatePost() {
        // Given
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setText("New content");
        request.setTags(Arrays.asList("tag1"));
        
        when(postRepository.create(any(Post.class))).thenReturn(testPost);

        // When
        Post result = postService.createPost(request);

        // Then
        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        
        verify(postRepository).create(any(Post.class));
    }

    @Test
    void testUpdatePost() {
        // Given
        UpdatePostRequest request = new UpdatePostRequest();
        request.setId(1L);
        request.setTitle("Updated Post");
        request.setText("Updated content");
        request.setTags(Arrays.asList("tag1"));
        
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.update(any(Post.class))).thenReturn(testPost);

        // When
        Post result = postService.updatePost(1L, request);

        // Then
        assertNotNull(result);
        
        verify(postRepository).findById(1L);
        verify(postRepository).update(any(Post.class));
    }

    @Test
    void testUpdatePostNotFound() {
        // Given
        UpdatePostRequest request = new UpdatePostRequest();
        request.setId(999L);
        request.setTitle("Updated Post");
        request.setText("Updated content");
        request.setTags(Arrays.asList("tag1"));
        
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            postService.updatePost(999L, request);
        });
        
        verify(postRepository).findById(999L);
        verify(postRepository, never()).update(any(Post.class));
    }

    @Test
    void testDeletePost() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // When
        postService.deletePost(1L);

        // Then
        verify(postRepository).delete(1L);
    }

    @Test
    void testIncrementLikes() {
        // Given
        Post likedPost = new Post();
        likedPost.setId(1L);
        likedPost.setTitle("Test Post");
        likedPost.setText("Test content");
        likedPost.setLikesCount(5);
        likedPost.setCommentsCount(0);
        
        when(postRepository.findById(1L)).thenReturn(Optional.of(likedPost));

        // When
        int likesCount = postService.incrementLikes(1L);

        // Then
        assertEquals(6, likesCount);
        
        verify(postRepository).incrementLikes(1L);
        verify(postRepository).findById(1L);
    }
}

