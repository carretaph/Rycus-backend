package com.rycus.Rycus_backend.post;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "post_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private String text;

    private String authorEmail;

    private String authorName;

    private Instant createdAt = Instant.now();

    public PostComment() {}

    public PostComment(Long postId, String text, String authorEmail, String authorName) {
        this.postId = postId;
        this.text = text;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getPostId() { return postId; }

    public String getText() { return text; }

    public String getAuthorEmail() { return authorEmail; }

    public String getAuthorName() { return authorName; }

    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }

    public void setPostId(Long postId) { this.postId = postId; }

    public void setText(String text) { this.text = text; }

    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}