package com.myblog.service;

import com.myblog.dto.CreateCommentRequest;
import com.myblog.dto.UpdateCommentRequest;
import com.myblog.model.Comment;
import com.myblog.repository.CommentRepository;
import com.myblog.service.impl.CommentServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment testComment;

    @BeforeEach
    void setUp() {
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setText("Test comment");
        testComment.setPostId(1L);
    }

    @Test
    void testGetCommentsByPostId() {
        // Given
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findByPostId(1L)).thenReturn(comments);

        // When
        List<Comment> result = commentService.getCommentsByPostId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getId(), result.get(0).getId());
        
        verify(commentRepository).findByPostId(1L);
    }

    @Test
    void testGetCommentById() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // When
        Optional<Comment> result = commentService.getCommentById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testComment.getId(), result.get().getId());
        
        verify(commentRepository).findById(1L);
    }

    @Test
    void testCreateComment() {
        // Given
        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("New comment");
        request.setPostId(1L);
        
        when(commentRepository.create(any(Comment.class))).thenReturn(testComment);

        // When
        Comment result = commentService.createComment(request);

        // Then
        assertNotNull(result);
        assertEquals(testComment.getId(), result.getId());
        
        verify(commentRepository).create(any(Comment.class));
    }

    @Test
    void testUpdateComment() {
        // Given
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setId(1L);
        request.setText("Updated comment");
        request.setPostId(1L);
        
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.update(any(Comment.class))).thenReturn(testComment);

        // When
        Comment result = commentService.updateComment(1L, request);

        // Then
        assertNotNull(result);
        
        verify(commentRepository).findById(1L);
        verify(commentRepository).update(any(Comment.class));
    }

    @Test
    void testUpdateCommentNotFound() {
        // Given
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setId(999L);
        request.setText("Updated comment");
        request.setPostId(1L);
        
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(999L, request);
        });
        
        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).update(any(Comment.class));
    }

    @Test
    void testDeleteComment() {        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // When
        commentService.deleteComment(1L);

        // Then
        verify(commentRepository).delete(1L);
    }
}

