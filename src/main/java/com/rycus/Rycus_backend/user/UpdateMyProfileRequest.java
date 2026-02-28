package com.rycus.Rycus_backend.user;

import java.math.BigDecimal;

public class UpdateMyProfileRequest {

    private String fullName;
    private String phone;
    private String avatarUrl;
    private String businessName;
    private String industry;
    private String city;
    private String state;

    // =========================================================
    // ✅ REFERRAL FEE (PUBLIC) - optional
    // =========================================================
    private Boolean offersReferralFee;      // true/false
    private String referralFeeType;         // "FLAT" | "PERCENT"
    private BigDecimal referralFeeValue;    // 50.00  | 10.00
    private String referralFeeNotes;        // <=255

    public UpdateMyProfileRequest() {}

    // ================================
    // Basic profile getters
    // ================================
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getBusinessName() { return businessName; }
    public String getIndustry() { return industry; }
    public String getCity() { return city; }
    public String getState() { return state; }

    // ================================
    // Basic profile setters
    // ================================
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setIndustry(String industry) { this.industry = industry; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }

    // ================================
    // ✅ Referral Fee getters/setters
    // ================================
    public Boolean getOffersReferralFee() { return offersReferralFee; }
    public void setOffersReferralFee(Boolean offersReferralFee) { this.offersReferralFee = offersReferralFee; }

    // normaliza a "FLAT" / "PERCENT" si viene con espacios o minúsculas
    public String getReferralFeeType() {
        if (referralFeeType == null) return null;
        String t = referralFeeType.trim().toUpperCase();
        return t.isEmpty() ? null : t;
    }
    public void setReferralFeeType(String referralFeeType) { this.referralFeeType = referralFeeType; }

    public BigDecimal getReferralFeeValue() { return referralFeeValue; }
    public void setReferralFeeValue(BigDecimal referralFeeValue) { this.referralFeeValue = referralFeeValue; }

    public String getReferralFeeNotes() {
        if (referralFeeNotes == null) return null;
        String n = referralFeeNotes.trim();
        return n.isEmpty() ? null : n;
    }
    public void setReferralFeeNotes(String referralFeeNotes) { this.referralFeeNotes = referralFeeNotes; }
}
