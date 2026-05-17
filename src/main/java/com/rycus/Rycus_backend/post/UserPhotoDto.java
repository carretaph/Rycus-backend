package com.rycus.Rycus_backend.post;

import java.time.Instant;

public class UserPhotoDto {

    private Long postId;
    private String imageUrl;
    private Instant createdAt;

    public UserPhotoDto() {}

    public UserPhotoDto(
            Long postId,
            String imageUrl,
            Instant createdAt
    ) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Long getPostId() {
        return postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}