package com.rycus.Rycus_backend.admin;

import com.rycus.Rycus_backend.user.User;

import java.time.Instant;

public class AdminUserDto {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String accountStatus;
    private String businessName;
    private String industry;
    private String city;
    private String state;
    private Boolean offersReferralFee;
    private String planType;
    private String subscriptionStatus;
    private Instant createdAt;

    public static AdminUserDto from(User user) {
        AdminUserDto dto = new AdminUserDto();

        dto.id = user.getId();
        dto.fullName = user.getFullName();
        dto.email = user.getEmail();
        dto.role = user.getRole();
        dto.accountStatus = user.getAccountStatus();
        dto.businessName = user.getBusinessName();
        dto.industry = user.getIndustry();
        dto.city = user.getCity();
        dto.state = user.getState();
        dto.offersReferralFee = user.getOffersReferralFee();
        dto.planType = user.getPlanType() == null ? null : user.getPlanType().name();
        dto.subscriptionStatus = user.getSubscriptionStatus();
        dto.createdAt = user.getCreatedAt();

        return dto;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAccountStatus() { return accountStatus; }
    public String getBusinessName() { return businessName; }
    public String getIndustry() { return industry; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public Boolean getOffersReferralFee() { return offersReferralFee; }
    public String getPlanType() { return planType; }
    public String getSubscriptionStatus() { return subscriptionStatus; }
    public Instant getCreatedAt() { return createdAt; }
}