package com.rycus.Rycus_backend.post;

import jakarta.persistence.*;

@Entity
@Table(name = "post_images")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    public PostImage() {}

    public PostImage(Long postId, String imageUrl) {
        this.postId = postId;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}