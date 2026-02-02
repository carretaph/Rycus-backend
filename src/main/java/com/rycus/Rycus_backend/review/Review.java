package com.rycus.Rycus_backend.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rycus.Rycus_backend.customer.Customer;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "reviews",
        indexes = {
                // ✅ usando nombres reales de columna en DB (snake_case)
                @Index(name = "idx_reviews_createdBy", columnList = "created_by"),
                @Index(name = "idx_reviews_customer", columnList = "customer_id"),
                @Index(name = "idx_reviews_createdBy_customer", columnList = "created_by, customer_id"),
                @Index(name = "idx_reviews_createdAt", columnList = "created_at")
        },
        uniqueConstraints = {
                // ✅ 1 review por customer por usuario
                @UniqueConstraint(name = "uk_reviews_createdBy_customer", columnNames = {"created_by", "customer_id"})
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int ratingOverall;
    private int ratingPayment;
    private int ratingBehavior;
    private int ratingCommunication;

    @Column(length = 2000)
    private String comment;

    // ✅ fuerza nombre de columna consistente con la DB
    @Column(name = "created_by", length = 180)
    private String createdBy;

    // ✅ ahora consistente con MilestoneService/Repo (timezone-safe)
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    // viene del frontend, no se guarda en BD
    @Transient
    private String userEmail;

    public Review() {}

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            // ✅ siempre UTC para evitar líos de zona horaria
            this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (this.createdBy != null) {
            this.createdBy = this.createdBy.trim();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getRatingOverall() { return ratingOverall; }
    public void setRatingOverall(int ratingOverall) { this.ratingOverall = ratingOverall; }

    public int getRatingPayment() { return ratingPayment; }
    public void setRatingPayment(int ratingPayment) { this.ratingPayment = ratingPayment; }

    public int getRatingBehavior() { return ratingBehavior; }
    public void setRatingBehavior(int ratingBehavior) { this.ratingBehavior = ratingBehavior; }

    public int getRatingCommunication() { return ratingCommunication; }
    public void setRatingCommunication(int ratingCommunication) { this.ratingCommunication = ratingCommunication; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
