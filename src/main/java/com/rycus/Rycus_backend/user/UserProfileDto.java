package com.rycus.Rycus_backend.user;

import java.util.List;

public class UserProfileDto {

    private Long id;
    private String fullName;
    private String email;
    private long totalReviews;
    private double averageRating;
    private List<UserReviewDto> reviews;

    public UserProfileDto() {
    }

    public UserProfileDto(Long id,
                          String fullName,
                          String email,
                          long totalReviews,
                          double averageRating,
                          List<UserReviewDto> reviews) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.totalReviews = totalReviews;
        this.averageRating = averageRating;
        this.reviews = reviews;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public List<UserReviewDto> getReviews() {
        return reviews;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTotalReviews(long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setReviews(List<UserReviewDto> reviews) {
        this.reviews = reviews;
    }
}
