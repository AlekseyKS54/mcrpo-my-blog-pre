package com.myblog.controller;

import com.myblog.dto.CreateCommentRequest;
import com.myblog.dto.UpdateCommentRequest;
import com.myblog.model.Comment;
import com.myblog.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
        log.debug("GET /api/posts/{}/comments", postId);
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        
        log.debug("GET /api/posts/{}/comments/{}", postId, commentId);
        Optional<Comment> comment = commentService.getCommentById(commentId);
        return comment.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request) {
        
        log.debug("POST /api/posts/{}/comments - text: {}", postId, request.getText());
        Comment createdComment = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request) {

        log.debug("PUT /api/posts/{}/comments/{} - text: {}", postId, commentId, request.getText());

        try {
            Comment updatedComment = commentService.updateComment(commentId, request);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        log.debug("DELETE /api/posts/{}/comments/{}", postId, commentId);

        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

