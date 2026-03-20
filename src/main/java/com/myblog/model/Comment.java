package com.myblog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private String text;
    private Long postId;
    
    @JsonIgnore
    private LocalDateTime createdAt;
    
    @JsonIgnore
    private LocalDateTime updatedAt;

    public Comment() {}

    public Comment(Long id, String text, Long postId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.text = text;
        this.postId = postId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getText() { return text; }
    public Long getPostId() { return postId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setPostId(Long postId) { this.postId = postId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
