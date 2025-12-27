package com.rycus.Rycus_backend.user;

public class UserSummaryDto {

    private Long id;
    private String fullName;
    private String email;
    private long totalReviews;
    private double averageRating;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id,
                          String fullName,
                          String email,
                          long totalReviews,
                          double averageRating) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.totalReviews = totalReviews;
        this.averageRating = averageRating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}
