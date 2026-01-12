package com.rycus.Rycus_backend.user;

import java.time.Instant;

public class SubscriptionStatusResponse {
    private boolean active;
    private PlanType planType;
    private Instant trialEndsAt;
    private Instant subscriptionEndsAt;
    private int freeMonthsBalance;
    private String referralCode;

    public SubscriptionStatusResponse() {}

    public SubscriptionStatusResponse(boolean active, PlanType planType, Instant trialEndsAt,
                                      Instant subscriptionEndsAt, int freeMonthsBalance, String referralCode) {
        this.active = active;
        this.planType = planType;
        this.trialEndsAt = trialEndsAt;
        this.subscriptionEndsAt = subscriptionEndsAt;
        this.freeMonthsBalance = freeMonthsBalance;
        this.referralCode = referralCode;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

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
}
