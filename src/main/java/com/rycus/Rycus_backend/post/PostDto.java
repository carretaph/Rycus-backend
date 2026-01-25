package com.rycus.Rycus_backend.post;

import java.time.Instant;

public class PostDto {

    private Long id;
    private String text;
    private String authorEmail;
    private String authorName;
    private Instant createdAt;

    private long likeCount;
    private boolean likedByViewer;

    public PostDto() {}

    public PostDto(Long id, String text, String authorEmail, String authorName, Instant createdAt,
                   long likeCount, boolean likedByViewer) {
        this.id = id;
        this.text = text;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.likedByViewer = likedByViewer;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getAuthorEmail() { return authorEmail; }
    public String getAuthorName() { return authorName; }
    public Instant getCreatedAt() { return createdAt; }

    public long getLikeCount() { return likeCount; }
    public boolean isLikedByViewer() { return likedByViewer; }
}
