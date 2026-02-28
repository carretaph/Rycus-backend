package com.rycus.Rycus_backend.user;

import java.math.BigDecimal;
import java.util.List;

public class UserProfileDto {

    private Long id;
    private String fullName;
    private String email;

    // ✅ public profile fields
    private String phone;
    private String businessName;
    private String industry;
    private String city;
    private String state;
    private String avatarUrl;

    // =========================================================
    // ✅ REFERRAL FEE (PUBLIC)
    // =========================================================
    private Boolean offersReferralFee;
    private String referralFeeType;       // "FLAT" | "PERCENT"
    private BigDecimal referralFeeValue;  // 50.00 | 10.00
    private String referralFeeNotes;      // <=255

    private long totalReviews;
    private double averageRating;
    private List<UserReviewDto> reviews;

    public UserProfileDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // ================================
    // ✅ Referral Fee getters/setters
    // ================================
    public Boolean getOffersReferralFee() { return offersReferralFee; }
    public void setOffersReferralFee(Boolean offersReferralFee) { this.offersReferralFee = offersReferralFee; }

    public String getReferralFeeType() { return referralFeeType; }
    public void setReferralFeeType(String referralFeeType) { this.referralFeeType = referralFeeType; }

    public BigDecimal getReferralFeeValue() { return referralFeeValue; }
    public void setReferralFeeValue(BigDecimal referralFeeValue) { this.referralFeeValue = referralFeeValue; }

    public String getReferralFeeNotes() { return referralFeeNotes; }
    public void setReferralFeeNotes(String referralFeeNotes) { this.referralFeeNotes = referralFeeNotes; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public List<UserReviewDto> getReviews() { return reviews; }
    public void setReviews(List<UserReviewDto> reviews) { this.reviews = reviews; }
}
