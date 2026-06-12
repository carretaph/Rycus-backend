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
                @Index(name = "idx_reviews_createdBy", columnList = "created_by"),
                @Index(name = "idx_reviews_customer", columnList = "customer_id"),
                @Index(name = "idx_reviews_createdBy_customer", columnList = "created_by, customer_id"),
                @Index(name = "idx_reviews_createdAt", columnList = "created_at")
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

    @Column(name = "created_by", length = 180)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    // ==========================
    // NUEVOS CAMPOS RYCUS
    // ==========================

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private ReviewOutcome outcome;

    @Column(name = "service_quoted", length = 150)
    private String serviceQuoted;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_not_sold")
    private ReasonNotSold reasonNotSold;

    // ==========================

    @Transient
    private String userEmail;

    public Review() {}

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }

        if (this.createdBy != null) {
            this.createdBy = this.createdBy.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRatingOverall() {
        return ratingOverall;
    }

    public void setRatingOverall(int ratingOverall) {
        this.ratingOverall = ratingOverall;
    }

    public int getRatingPayment() {
        return ratingPayment;
    }

    public void setRatingPayment(int ratingPayment) {
        this.ratingPayment = ratingPayment;
    }

    public int getRatingBehavior() {
        return ratingBehavior;
    }

    public void setRatingBehavior(int ratingBehavior) {
        this.ratingBehavior = ratingBehavior;
    }

    public int getRatingCommunication() {
        return ratingCommunication;
    }

    public void setRatingCommunication(int ratingCommunication) {
        this.ratingCommunication = ratingCommunication;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ReviewOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(ReviewOutcome outcome) {
        this.outcome = outcome;
    }

    public String getServiceQuoted() {
        return serviceQuoted;
    }

    public void setServiceQuoted(String serviceQuoted) {
        this.serviceQuoted = serviceQuoted;
    }

    public ReasonNotSold getReasonNotSold() {
        return reasonNotSold;
    }

    public void setReasonNotSold(ReasonNotSold reasonNotSold) {
        this.reasonNotSold = reasonNotSold;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}