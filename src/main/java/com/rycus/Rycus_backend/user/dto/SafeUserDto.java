package com.rycus.Rycus_backend.user.dto;

import com.rycus.Rycus_backend.user.User;

public class SafeUserDto {

    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;

    private String phone;
    private String businessName;
    private String city;
    private String state;

    public SafeUserDto() {}

    public static SafeUserDto from(User u) {
        SafeUserDto dto = new SafeUserDto();
        dto.id = u.getId();
        dto.email = u.getEmail();
        dto.fullName = u.getFullName();
        dto.avatarUrl = u.getAvatarUrl();
        dto.phone = u.getPhone();
        dto.businessName = u.getBusinessName();
        dto.city = u.getCity();
        dto.state = u.getState();
        return dto;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPhone() { return phone; }
    public String getBusinessName() { return businessName; }
    public String getCity() { return city; }
    public String getState() { return state; }
}
