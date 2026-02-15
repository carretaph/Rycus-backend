package com.rycus.Rycus_backend.post;

import java.time.Instant;

public class PostDto {

    private Long id;
    private String text;

    private String authorEmail;
    private String authorName;

    // ✅ AVATAR
    private String authorAvatarUrl;

    private Instant createdAt;

    private long likeCount;
    private boolean likedByViewer;

    // =====================================================
    // CONSTRUCTOR VACÍO (Jackson lo necesita)
    // =====================================================
    public PostDto() {}

    // =====================================================
    // CONSTRUCTOR COMPLETO
    // =====================================================
    public PostDto(
            Long id,
            String text,
            String authorEmail,
            String authorName,
            String authorAvatarUrl,
            Instant createdAt,
            long likeCount,
            boolean likedByViewer
    ) {
        this.id = id;
        this.text = text;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.likedByViewer = likedByViewer;
    }

    // =====================================================
    // GETTERS
    // =====================================================
    public Long getId() { return id; }
    public String getText() { return text; }
    public String getAuthorEmail() { return authorEmail; }
    public String getAuthorName() { return authorName; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public long getLikeCount() { return likeCount; }
    public boolean isLikedByViewer() { return likedByViewer; }

    // =====================================================
    // SETTERS (recomendado agregarlos)
    // =====================================================
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public void setLikedByViewer(boolean likedByViewer) { this.likedByViewer = likedByViewer; }
}
