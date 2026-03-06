package com.rycus.Rycus_backend.post;

import java.time.Instant;

public class CommentDto {
    private Long id;
    private Long postId;
    private String text;
    private String authorEmail;
    private String authorName;
    private String authorAvatarUrl;
    private Instant createdAt;

    public CommentDto() {}

    public CommentDto(Long id, Long postId, String text, String authorEmail, String authorName, String authorAvatarUrl, Instant createdAt) {
        this.id = id;
        this.postId = postId;
        this.text = text;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public String getText() { return text; }
    public String getAuthorEmail() { return authorEmail; }
    public String getAuthorName() { return authorName; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setPostId(Long postId) { this.postId = postId; }
    public void setText(String text) { this.text = text; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}