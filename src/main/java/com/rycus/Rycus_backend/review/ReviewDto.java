package com.rycus.Rycus_backend.review;

import java.time.OffsetDateTime;

public class ReviewDto {
    private Long id;

    private Long customerId;
    private String customerName;

    private Integer ratingOverall;
    private Integer ratingPayment;
    private Integer ratingBehavior;
    private Integer ratingCommunication;

    private String comment;
    private OffsetDateTime createdAt;

    public ReviewDto() {}

    public ReviewDto(Review r) {
        this.id = r.getId();

        // ✅ importante: esto exige que el repo use JOIN FETCH customer
        if (r.getCustomer() != null) {
            this.customerId = r.getCustomer().getId();
            this.customerName = r.getCustomer().getFullName();
        }

        this.ratingOverall = r.getRatingOverall();
        this.ratingPayment = r.getRatingPayment();
        this.ratingBehavior = r.getRatingBehavior();
        this.ratingCommunication = r.getRatingCommunication();

        this.comment = r.getComment();
        this.createdAt = r.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }

    public Integer getRatingOverall() { return ratingOverall; }
    public Integer getRatingPayment() { return ratingPayment; }
    public Integer getRatingBehavior() { return ratingBehavior; }
    public Integer getRatingCommunication() { return ratingCommunication; }

    public String getComment() { return comment; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public void setRatingOverall(Integer ratingOverall) { this.ratingOverall = ratingOverall; }
    public void setRatingPayment(Integer ratingPayment) { this.ratingPayment = ratingPayment; }
    public void setRatingBehavior(Integer ratingBehavior) { this.ratingBehavior = ratingBehavior; }
    public void setRatingCommunication(Integer ratingCommunication) { this.ratingCommunication = ratingCommunication; }

    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
