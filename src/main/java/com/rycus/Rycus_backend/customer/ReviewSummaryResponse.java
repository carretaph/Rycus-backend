package com.rycus.Rycus_backend.customer;

public class ReviewSummaryResponse {

    private Long customerId;
    private long totalReviews;
    private double averageRatingPayment;
    private double averageRatingBehavior;
    private double averageOverall;

    public ReviewSummaryResponse() {
    }

    public ReviewSummaryResponse(Long customerId,
                                 long totalReviews,
                                 double averageRatingPayment,
                                 double averageRatingBehavior,
                                 double averageOverall) {
        this.customerId = customerId;
        this.totalReviews = totalReviews;
        this.averageRatingPayment = averageRatingPayment;
        this.averageRatingBehavior = averageRatingBehavior;
        this.averageOverall = averageOverall;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public double getAverageRatingPayment() {
        return averageRatingPayment;
    }

    public void setAverageRatingPayment(double averageRatingPayment) {
        this.averageRatingPayment = averageRatingPayment;
    }

    public double getAverageRatingBehavior() {
        return averageRatingBehavior;
    }

    public void setAverageRatingBehavior(double averageRatingBehavior) {
        this.averageRatingBehavior = averageRatingBehavior;
    }

    public double getAverageOverall() {
        return averageOverall;
    }

    public void setAverageOverall(double averageOverall) {
        this.averageOverall = averageOverall;
    }
}
