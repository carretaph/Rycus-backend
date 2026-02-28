package com.rycus.Rycus_backend.user;

import java.math.BigDecimal;

public class AuthRequest {

    // Compatibilidad: algunas pantallas mandan "name", otras "fullName"
    private String name;
    private String fullName;

    private String email;
    private String password;

    // NEW
    private String phone;

    // =========================================================
    // REFERRAL FEE (PUBLIC) - optional at registration
    // =========================================================
    private Boolean offersReferralFee;     // true/false
    private String referralFeeType;        // "FLAT" | "PERCENT"
    private BigDecimal referralFeeValue;  // 50.00  | 10.00
    private String referralFeeNotes;       // short notes (<=255)

    public AuthRequest() {}

    // ================================
    // Name helpers
    // ================================
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEffectiveName() {
        if (name != null && !name.trim().isEmpty()) return name.trim();
        if (fullName != null && !fullName.trim().isEmpty()) return fullName.trim();
        return null;
    }

    // ================================
    // Email / Password
    // ================================
    // ✅ Si algún día necesitas el email tal como llegó
    public String getEmailRaw() { return email; }

    // ✅ NORMALIZADO: trim + lowercase
    public String getEmail() {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public void setEmail(String email) { this.email = email; }

    // ✅ (opcional) evita errores por espacios al copiar/pegar
    public String getPassword() {
        if (password == null) return null;
        return password.trim();
    }

    public void setPassword(String password) { this.password = password; }

    // ================================
    // Phone
    // ================================
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // ================================
    // Referral Fee getters/setters
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
