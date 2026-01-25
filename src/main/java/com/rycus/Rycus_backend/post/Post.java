package com.rycus.Rycus_backend.post;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4000)
    private String text;

    @Column(nullable = false, length = 255)
    private String authorEmail;

    @Column(nullable = false, length = 255)
    private String authorName;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Post() {}

    public Post(String text, String authorEmail, String authorName) {
        this.text = text;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
