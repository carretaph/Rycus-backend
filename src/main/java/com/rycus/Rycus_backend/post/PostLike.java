package com.rycus.Rycus_backend.post;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"post_id", "user_email"})
        }
)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public PostLike() {}

    public PostLike(Post post, String userEmail) {
        this.post = post;
        this.userEmail = userEmail;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public String getUserEmail() { return userEmail; }
    public Instant getCreatedAt() { return createdAt; }
}
