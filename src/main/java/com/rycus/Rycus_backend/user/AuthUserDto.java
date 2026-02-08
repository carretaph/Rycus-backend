package com.rycus.Rycus_backend.user;

import java.time.OffsetDateTime;

public class AuthUserDto {

    private Long id;
    private String email;
    private String name;
    private String phone;

    // ✅ NUEVO: fields que el frontend necesita para no mandar a "Unlock"
    private String role;
    private String planType;
    private String subscriptionStatus;

    private OffsetDateTime trialEndsAt;
    private OffsetDateTime accessEndsAt;
    private OffsetDateTime subscriptionEndsAt;

    private Integer freeMonthsBalance;

    public AuthUserDto() {}

    public AuthUserDto(
            Long id,
            String email,
            String name,
            String phone,
            String role,
            String planType,
            String subscriptionStatus,
            OffsetDateTime trialEndsAt,
            OffsetDateTime accessEndsAt,
            OffsetDateTime subscriptionEndsAt,
            Integer freeMonthsBalance
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

        this.freeMonthsBalance = freeMonthsBalance;
    }

    public static AuthUserDto from(User user) {
        if (user == null) return null;

        // PlanType puede ser enum o String dependiendo de tu entity
        String planTypeStr = null;
        try {
            Object pt = user.getPlanType();
            if (pt != null) planTypeStr = pt.toString(); // enum.name() o String directo
        } catch (Exception ignored) {}

        Integer freeMonths = null;
        try {
            freeMonths = user.getFreeMonthsBalance();
        } catch (Exception ignored) {}

        String role = user.getRole();
        if (role == null || role.isBlank()) role = "USER";

        return new AuthUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),

                role.trim(),
                planTypeStr,
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

    public OffsetDateTime getTrialEndsAt() { return trialEndsAt; }
    public OffsetDateTime getAccessEndsAt() { return accessEndsAt; }
    public OffsetDateTime getSubscriptionEndsAt() { return subscriptionEndsAt; }

    public Integer getFreeMonthsBalance() { return freeMonthsBalance; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setRole(String role) { this.role = role; }
    public void setPlanType(String planType) { this.planType = planType; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public void setTrialEndsAt(OffsetDateTime trialEndsAt) { this.trialEndsAt = trialEndsAt; }
    public void setAccessEndsAt(OffsetDateTime accessEndsAt) { this.accessEndsAt = accessEndsAt; }
    public void setSubscriptionEndsAt(OffsetDateTime subscriptionEndsAt) { this.subscriptionEndsAt = subscriptionEndsAt; }

    public void setFreeMonthsBalance(Integer freeMonthsBalance) { this.freeMonthsBalance = freeMonthsBalance; }
}
