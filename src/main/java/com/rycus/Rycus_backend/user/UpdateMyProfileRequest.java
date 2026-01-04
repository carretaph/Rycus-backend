package com.rycus.Rycus_backend.user;

public class UpdateMyProfileRequest {

    private String fullName;
    private String phone;
    private String avatarUrl;
    private String businessName;
    private String industry;
    private String city;
    private String state;

    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getBusinessName() { return businessName; }
    public String getIndustry() { return industry; }
    public String getCity() { return city; }
    public String getState() { return state; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setIndustry(String industry) { this.industry = industry; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
}
