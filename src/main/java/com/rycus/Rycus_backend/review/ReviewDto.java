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

    // NUEVOS CAMPOS
    private ReviewOutcome outcome;
    private String serviceQuoted;
    private ReasonNotSold reasonNotSold;

    public ReviewDto() {}

    public ReviewDto(Review r) {
        this.id = r.getId();

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

        this.outcome = r.getOutcome();
        this.serviceQuoted = r.getServiceQuoted();
        this.reasonNotSold = r.getReasonNotSold();
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

    public ReviewOutcome getOutcome() { return outcome; }
    public String getServiceQuoted() { return serviceQuoted; }
    public ReasonNotSold getReasonNotSold() { return reasonNotSold; }

    public void setId(Long id) { this.id = id; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public void setRatingOverall(Integer ratingOverall) { this.ratingOverall = ratingOverall; }
    public void setRatingPayment(Integer ratingPayment) { this.ratingPayment = ratingPayment; }
    public void setRatingBehavior(Integer ratingBehavior) { this.ratingBehavior = ratingBehavior; }
    public void setRatingCommunication(Integer ratingCommunication) { this.ratingCommunication = ratingCommunication; }

    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public void setOutcome(ReviewOutcome outcome) { this.outcome = outcome; }
    public void setServiceQuoted(String serviceQuoted) { this.serviceQuoted = serviceQuoted; }
    public void setReasonNotSold(ReasonNotSold reasonNotSold) { this.reasonNotSold = reasonNotSold; }
}