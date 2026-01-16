// src/main/java/com/rycus/Rycus_backend/user/User.java
package com.rycus.Rycus_backend.user;

import jakarta.persistence.*;

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
                @Index(name = "idx_users_createdAt", columnList = "created_at")
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

    // ✅ profile fields
    private String phone;

    @Column(name = "business_name")
    private String businessName;

    private String industry;
    private String city;
    private String state;

    // ✅ Postgres: TEXT (NO @Lob => evita CLOB)
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl; // base64 o url

    // =========================================================
    // ✅ ACCOUNT CREATED AT (promo 3 meses)
    // =========================================================
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // =========================================================
    // ✅ PAYMENTS / SUBSCRIPTIONS
    // =========================================================

    // FREE_TRIAL (1 mes), PAID, FREE_LIFETIME, EXPIRED
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", length = 30, nullable = false)
    private PlanType planType = PlanType.FREE_TRIAL;

    // fin del trial (por default: now + 30d en register)
    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;

    // hasta cuándo tiene acceso (trial o pagado o con créditos)
    @Column(name = "subscription_ends_at")
    private Instant subscriptionEndsAt;

    // meses gratis acumulados (referidos/rewards)
    @Column(name = "free_months_balance", nullable = false)
    private int freeMonthsBalance = 0;

    // =========================================================
    // ✅ REFERRALS
    // =========================================================

    @Column(name = "referral_code", length = 40, unique = true)
    private String referralCode;

    // quién me refirió (email) - simple y consistente con tu app
    @Column(name = "referred_by_email", length = 180)
    private String referredByEmail;

    // =========================================================
    // ✅ Stripe (para futuro)
    // =========================================================
    @Column(name = "stripe_customer_id", length = 80)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 80)
    private String stripeSubscriptionId;

    public User() {}

    // =========================
    // Helpers de negocio (opcionales, pero útiles)
    // =========================
    public boolean isLifetimeFree() {
        return this.planType == PlanType.FREE_LIFETIME;
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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public Instant getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(Instant trialEndsAt) { this.trialEndsAt = trialEndsAt; }

    public Instant getSubscriptionEndsAt() { return subscriptionEndsAt; }
    public void setSubscriptionEndsAt(Instant subscriptionEndsAt) { this.subscriptionEndsAt = subscriptionEndsAt; }

    public int getFreeMonthsBalance() { return freeMonthsBalance; }
    public void setFreeMonthsBalance(int freeMonthsBalance) { this.freeMonthsBalance = freeMonthsBalance; }

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public String getReferredByEmail() { return referredByEmail; }
    public void setReferredByEmail(String referredByEmail) { this.referredByEmail = referredByEmail; }

    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String stripeCustomerId) { this.stripeCustomerId = stripeCustomerId; }

    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public void setStripeSubscriptionId(String stripeSubscriptionId) { this.stripeSubscriptionId = stripeSubscriptionId; }
}
