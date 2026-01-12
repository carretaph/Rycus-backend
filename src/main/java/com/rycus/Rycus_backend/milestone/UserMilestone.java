package com.rycus.Rycus_backend.milestone;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_milestones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_milestones_user_type",
                        columnNames = {"user_email", "milestone_type"}
                )
        }
)
public class UserMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 180)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "milestone_type", nullable = false, length = 60)
    private MilestoneType milestoneType;

    @Column(name = "times_awarded", nullable = false)
    private int timesAwarded = 0;

    @Column(name = "last_awarded_at")
    private OffsetDateTime lastAwardedAt;

    // =====================
    // Getters / Setters
    // =====================

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public MilestoneType getMilestoneType() {
        return milestoneType;
    }

    public void setMilestoneType(MilestoneType milestoneType) {
        this.milestoneType = milestoneType;
    }

    public int getTimesAwarded() {
        return timesAwarded;
    }

    public void setTimesAwarded(int timesAwarded) {
        this.timesAwarded = timesAwarded;
    }

    public OffsetDateTime getLastAwardedAt() {
        return lastAwardedAt;
    }

    public void setLastAwardedAt(OffsetDateTime lastAwardedAt) {
        this.lastAwardedAt = lastAwardedAt;
    }
}
