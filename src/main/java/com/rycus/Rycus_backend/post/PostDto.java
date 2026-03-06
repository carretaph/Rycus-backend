package com.rycus.Rycus_backend.post;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    // ✅ NEW
    private long commentCount;
    private List<String> imageUrls = new ArrayList<>(); // 0..6

    // =====================================================
    // CONSTRUCTOR VACÍO (Jackson lo necesita)
    // =====================================================
    public PostDto() {}

    // =====================================================
    // CONSTRUCTOR (COMPATIBLE) - el que ya usas en tu service
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

        // defaults
        this.commentCount = 0L;
        this.imageUrls = new ArrayList<>();
    }

    // =====================================================
    // CONSTRUCTOR COMPLETO (NEW)
    // =====================================================
    public PostDto(
            Long id,
            String text,
            String authorEmail,
            String authorName,
            String authorAvatarUrl,
            Instant createdAt,
            long likeCount,
            boolean likedByViewer,
            long commentCount,
            List<String> imageUrls
    ) {
        this.id = id;
        this.text = text;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.likedByViewer = likedByViewer;
        this.commentCount = commentCount;
        this.imageUrls = (imageUrls == null) ? new ArrayList<>() : imageUrls;
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

    public long getCommentCount() { return commentCount; }
    public List<String> getImageUrls() { return imageUrls; }

    // =====================================================
    // SETTERS
    // =====================================================
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public void setLikedByViewer(boolean likedByViewer) { this.likedByViewer = likedByViewer; }

    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls == null) ? new ArrayList<>() : imageUrls;
    }
}