package com.rycus.Rycus_backend.user.dto;

public class UserMiniDto {

    private Long id;
    private String fullName;
    private String email;
    private String avatarUrl;

    // ✅ Constructor 4 params (para JPQL new UserMiniDto(u.id, u.fullName, u.email, u.avatarUrl))
    public UserMiniDto(Long id, String fullName, String email, String avatarUrl) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    // ✅ Constructor 3 params (por si en algún lado tienes JPQL con 3 args)
    public UserMiniDto(String email, String fullName, String avatarUrl) {
        this.email = email;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

    public UserMiniDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
