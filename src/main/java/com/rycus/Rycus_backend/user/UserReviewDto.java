package com.rycus.Rycus_backend.user;

public class UserReviewDto {

    private Long id;
    private Long customerId;
    private String customerName;
    private Integer ratingOverall;
    private Integer ratingPayment;
    private Integer ratingBehavior;
    private Integer ratingCommunication;
    private String comment;
    private String createdAt; // lo devolvemos como texto ISO

    public UserReviewDto() {
    }

    public UserReviewDto(
            Long id,
            Long customerId,
            String customerName,
            Integer ratingOverall,
            Integer ratingPayment,
            Integer ratingBehavior,
            Integer ratingCommunication,
            String comment,
            String createdAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.ratingOverall = ratingOverall;
        this.ratingPayment = ratingPayment;
        this.ratingBehavior = ratingBehavior;
        this.ratingCommunication = ratingCommunication;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Integer getRatingOverall() {
        return ratingOverall;
    }

    public Integer getRatingPayment() {
        return ratingPayment;
    }

    public Integer getRatingBehavior() {
        return ratingBehavior;
    }

    public Integer getRatingCommunication() {
        return ratingCommunication;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setRatingOverall(Integer ratingOverall) {
        this.ratingOverall = ratingOverall;
    }

    public void setRatingPayment(Integer ratingPayment) {
        this.ratingPayment = ratingPayment;
    }

    public void setRatingBehavior(Integer ratingBehavior) {
        this.ratingBehavior = ratingBehavior;
    }

    public void setRatingCommunication(Integer ratingCommunication) {
        this.ratingCommunication = ratingCommunication;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
