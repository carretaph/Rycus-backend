// src/main/java/com/rycus/Rycus_backend/user/dto/SafeUserDto.java
package com.rycus.Rycus_backend.user.dto;

import com.rycus.Rycus_backend.user.User;

import java.math.BigDecimal;

public class SafeUserDto {

    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String businessName;
    private String city;
    private String state;

    private String planType;
    private String subscriptionStatus;

    // ✅ Referral fee fields
    private Boolean offersReferralFee;
    private String referralFeeType;
    private BigDecimal referralFeeValue;
    private String referralFeeNotes;

    public SafeUserDto() {}

    public SafeUserDto(
            Long id,
            String email,
            String fullName,
            String avatarUrl,
            String phone,
            String businessName,
            String city,
            String state,
            String planType,
            String subscriptionStatus,
            Boolean offersReferralFee,
            String referralFeeType,
            BigDecimal referralFeeValue,
            String referralFeeNotes
    ) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.businessName = businessName;
        this.city = city;
        this.state = state;
        this.planType = planType;
        this.subscriptionStatus = subscriptionStatus;

        this.offersReferralFee = offersReferralFee;
        this.referralFeeType = referralFeeType;
        this.referralFeeValue = referralFeeValue;
        this.referralFeeNotes = referralFeeNotes;
    }

    public static SafeUserDto from(User user) {
        if (user == null) return null;

        return new SafeUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getPhone(),
                user.getBusinessName(),
                user.getCity(),
                user.getState(),
                user.getPlanType() == null ? null : user.getPlanType().name(),
                user.getSubscriptionStatus(),

                // ✅ referral fee
                user.getOffersReferralFee(),
                user.getReferralFeeType(),
                user.getReferralFeeValue(),
                user.getReferralFeeNotes()
        );
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPhone() { return phone; }
    public String getBusinessName() { return businessName; }
    public String getCity() { return city; }
    public String getState() { return state; }

    public String getPlanType() { return planType; }
    public String getSubscriptionStatus() { return subscriptionStatus; }

    public Boolean getOffersReferralFee() { return offersReferralFee; }
    public String getReferralFeeType() { return referralFeeType; }
    public BigDecimal getReferralFeeValue() { return referralFeeValue; }
    public String getReferralFeeNotes() { return referralFeeNotes; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }

    public void setPlanType(String planType) { this.planType = planType; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public void setOffersReferralFee(Boolean offersReferralFee) { this.offersReferralFee = offersReferralFee; }
    public void setReferralFeeType(String referralFeeType) { this.referralFeeType = referralFeeType; }
    public void setReferralFeeValue(BigDecimal referralFeeValue) { this.referralFeeValue = referralFeeValue; }
    public void setReferralFeeNotes(String referralFeeNotes) { this.referralFeeNotes = referralFeeNotes; }
}