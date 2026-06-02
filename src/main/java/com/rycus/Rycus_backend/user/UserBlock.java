package com.rycus.Rycus_backend.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_blocks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_block_pair",
                        columnNames = {"blocker_id", "blocked_id"}
                )
        }
)
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que bloquea
    @ManyToOne(optional = false)
    @JoinColumn(name = "blocker_id")
    private User blocker;

    // Usuario bloqueado
    @ManyToOne(optional = false)
    @JoinColumn(name = "blocked_id")
    private User blocked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public UserBlock() {}

    public UserBlock(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public User getBlocker() {
        return blocker;
    }

    public void setBlocker(User blocker) {
        this.blocker = blocker;
    }

    public User getBlocked() {
        return blocked;
    }

    public void setBlocked(User blocked) {
        this.blocked = blocked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}