package com.myblog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Post {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private Integer likesCount;
    private Integer commentsCount;
    
    @JsonIgnore
    private LocalDateTime createdAt;
    
    @JsonIgnore
    private LocalDateTime updatedAt;

    public Post() {
        this.tags = new ArrayList<>();
    }

    public Post(Long id, String title, String text, Integer likesCount, Integer commentsCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = new ArrayList<>();
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public List<String> getTags() { return tags; }
    public Integer getLikesCount() { return likesCount; }
    public Integer getCommentsCount() { return commentsCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
