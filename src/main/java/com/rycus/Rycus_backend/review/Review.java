package com.rycus.Rycus_backend.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rycus.Rycus_backend.customer.Customer;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
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

    @Column(length = 180)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

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
            this.createdAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
