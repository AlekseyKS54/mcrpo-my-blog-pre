package com.myblog.service.impl;

import com.myblog.dto.CreateCommentRequest;
import com.myblog.dto.UpdateCommentRequest;
import com.myblog.model.Comment;
import com.myblog.repository.CommentRepository;
import com.myblog.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        log.debug("Getting comments for post with id: {}", postId);
        return commentRepository.findByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comment> getCommentById(Long commentId) {
        log.debug("Getting comment by id: {}", commentId);
        return commentRepository.findById(commentId);
    }

    @Override
    @Transactional
    public Comment createComment(CreateCommentRequest request) {
        log.debug("Creating new comment for post with id: {}", request.getPostId());
        
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setPostId(request.getPostId());
        
        return commentRepository.create(comment);
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, UpdateCommentRequest request) {
        log.debug("Updating comment with id: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        comment.setText(request.getText());
        return commentRepository.update(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        log.debug("Deleting comment with id: {}", commentId);

        commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        commentRepository.delete(commentId);
    }
}

