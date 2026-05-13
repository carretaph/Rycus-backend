// src/main/java/com/rycus/Rycus_backend/user/User.java
package com.rycus.Rycus_backend.user;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_planType", columnList = "plan_type"),
                @Index(name = "idx_users_subscriptionEndsAt", columnList = "subscription_ends_at"),
                @Index(name = "idx_users_referralCode", columnList = "referral_code"),
                @Index(name = "idx_users_referredBy", columnList = "referred_by_email"),
                @Index(name = "idx_users_createdAt", columnList = "created_at"),
                @Index(name = "idx_users_stripeCustomerId", columnList = "stripe_customer_id"),
                @Index(name = "idx_users_offersReferralFee", columnList = "offers_referral_fee")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(unique = true, length = 180, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; // USER, ADMIN

    @Column(name = "account_status", length = 20)
    private String accountStatus = "ACTIVE";

    private String phone;

    @Column(name = "business_name")
    private String businessName;

    private String industry;
    private String city;
    private String state;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "offers_referral_fee", nullable = false)
    private Boolean offersReferralFee = false;

    @Column(name = "referral_fee_type", length = 10)
    private String referralFeeType;

    @Column(name = "referral_fee_value", precision = 10, scale = 2)
    private BigDecimal referralFeeValue;

    @Column(name = "referral_fee_notes", length = 500)
    private String referralFeeNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 30, nullable = false)
    private PlanType planType = PlanType.FREE_TRIAL;

    @Column(name = "subscription_status", length = 40)
    private String subscriptionStatus;

    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;

    @Column(name = "subscription_ends_at")
    private Instant subscriptionEndsAt;

    @Column(name = "access_ends_at")
    private Instant accessEndsAt;

    @Column(name = "free_months_balance", nullable = false)
    private int freeMonthsBalance = 0;

    @Column(name = "referral_code", length = 40, unique = true)
    private String referralCode;

    @Column(name = "referred_by_email", length = 180)
    private String referredByEmail;

    @Column(name = "stripe_customer_id", length = 80)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 80)
    private String stripeSubscriptionId;

    public User() {}

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }

        if (this.planType == null) {
            this.planType = PlanType.FREE_TRIAL;
        }

        if (this.offersReferralFee == null) {
            this.offersReferralFee = false;
        }

        if (this.accountStatus == null || this.accountStatus.isBlank()) {
            this.accountStatus = "ACTIVE";
        }
    }

    public boolean isLifetimeFree() {
        return this.planType == PlanType.FREE_LIFETIME;
    }

    public boolean hasActiveAccess() {
        if ("SUSPENDED".equalsIgnoreCase(accountStatus)) return false;
        if ("BANNED".equalsIgnoreCase(accountStatus)) return false;

        if (isLifetimeFree()) return true;

        boolean stripeOk =
                "trialing".equalsIgnoreCase(subscriptionStatus) ||
                        "active".equalsIgnoreCase(subscriptionStatus);

        boolean timeOk =
                accessEndsAt != null && Instant.now().isBefore(accessEndsAt);

        return stripeOk || timeOk;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

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

    public Boolean getOffersReferralFee() { return offersReferralFee; }
    public void setOffersReferralFee(Boolean offersReferralFee) { this.offersReferralFee = offersReferralFee; }

    public String getReferralFeeType() { return referralFeeType; }
    public void setReferralFeeType(String referralFeeType) { this.referralFeeType = referralFeeType; }

    public BigDecimal getReferralFeeValue() { return referralFeeValue; }
    public void setReferralFeeValue(BigDecimal referralFeeValue) { this.referralFeeValue = referralFeeValue; }

    public String getReferralFeeNotes() { return referralFeeNotes; }
    public void setReferralFeeNotes(String referralFeeNotes) { this.referralFeeNotes = referralFeeNotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public Instant getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(Instant trialEndsAt) { this.trialEndsAt = trialEndsAt; }

    public Instant getSubscriptionEndsAt() { return subscriptionEndsAt; }
    public void setSubscriptionEndsAt(Instant subscriptionEndsAt) {
        this.subscriptionEndsAt = subscriptionEndsAt;
    }

    public Instant getAccessEndsAt() { return accessEndsAt; }
    public void setAccessEndsAt(Instant accessEndsAt) { this.accessEndsAt = accessEndsAt; }

    public int getFreeMonthsBalance() { return freeMonthsBalance; }
    public void setFreeMonthsBalance(int freeMonthsBalance) {
        this.freeMonthsBalance = freeMonthsBalance;
    }

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public String getReferredByEmail() { return referredByEmail; }
    public void setReferredByEmail(String referredByEmail) {
        this.referredByEmail = referredByEmail;
    }

    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }
}