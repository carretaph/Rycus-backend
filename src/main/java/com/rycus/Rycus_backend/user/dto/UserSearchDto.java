package com.rycus.Rycus_backend.user.dto;

import java.math.BigDecimal;

public class UserSearchDto {

    private Long id;
    private String fullName;
    private String email;
    private String avatarUrl;

    private Boolean offersReferralFee;
    private String referralFeeType;
    private BigDecimal referralFeeValue;
    private String referralFeeNotes;

    public UserSearchDto(
            Long id,
            String fullName,
            String email,
            String avatarUrl,
            Boolean offersReferralFee,
            String referralFeeType,
            BigDecimal referralFeeValue,
            String referralFeeNotes
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.offersReferralFee = offersReferralFee;
        this.referralFeeType = referralFeeType;
        this.referralFeeValue = referralFeeValue;
        this.referralFeeNotes = referralFeeNotes;
    }

    public UserSearchDto() {}

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }

    public Boolean getOffersReferralFee() { return offersReferralFee; }
    public String getReferralFeeType() { return referralFeeType; }
    public BigDecimal getReferralFeeValue() { return referralFeeValue; }
    public String getReferralFeeNotes() { return referralFeeNotes; }
}
