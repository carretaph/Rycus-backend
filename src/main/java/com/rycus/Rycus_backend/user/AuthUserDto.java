package com.rycus.Rycus_backend.user;

import java.time.Instant;

public class AuthUserDto {

    private Long id;
    private String email;
    private String name;
    private String phone;

    private String role;
    private String planType;
    private String subscriptionStatus;

    private Instant trialEndsAt;
    private Instant accessEndsAt;
    private Instant subscriptionEndsAt;

    private Integer freeMonths;

    public AuthUserDto() {}

    public AuthUserDto(
            Long id,
            String email,
            String name,
            String phone,
            String role,
            String planType,
            String subscriptionStatus,
            Instant trialEndsAt,
            Instant accessEndsAt,
            Instant subscriptionEndsAt,
            Integer freeMonths
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.planType = planType;
        this.subscriptionStatus = subscriptionStatus;
        this.trialEndsAt = trialEndsAt;
        this.accessEndsAt = accessEndsAt;
        this.subscriptionEndsAt = subscriptionEndsAt;
        this.freeMonths = freeMonths;
    }

    public static AuthUserDto from(User user) {
        if (user == null) return null;

        String role = (user.getRole() == null || user.getRole().isBlank()) ? "USER" : user.getRole().trim();
        String planType = (user.getPlanType() == null) ? null : user.getPlanType().name();

        // ✅ si tu entity lo tiene como int, esto está perfecto
        Integer freeMonths = user.getFreeMonthsBalance();

        return new AuthUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                role,
                planType,
                user.getSubscriptionStatus(),
                user.getTrialEndsAt(),
                user.getAccessEndsAt(),
                user.getSubscriptionEndsAt(),
                freeMonths
        );
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    public String getRole() { return role; }
    public String getPlanType() { return planType; }
    public String getSubscriptionStatus() { return subscriptionStatus; }

    public Instant getTrialEndsAt() { return trialEndsAt; }
    public Instant getAccessEndsAt() { return accessEndsAt; }
    public Instant getSubscriptionEndsAt() { return subscriptionEndsAt; }

    public Integer getFreeMonths() { return freeMonths; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setRole(String role) { this.role = role; }
    public void setPlanType(String planType) { this.planType = planType; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public void setTrialEndsAt(Instant trialEndsAt) { this.trialEndsAt = trialEndsAt; }
    public void setAccessEndsAt(Instant accessEndsAt) { this.accessEndsAt = accessEndsAt; }
    public void setSubscriptionEndsAt(Instant subscriptionEndsAt) { this.subscriptionEndsAt = subscriptionEndsAt; }

    public void setFreeMonths(Integer freeMonths) { this.freeMonths = freeMonths; }
}
