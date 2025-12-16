package com.rycus.Rycus_backend.user;

public class AuthUserDto {

    private Long id;
    private String email;
    private String name;
    private String phone;

    public AuthUserDto() {}

    public AuthUserDto(Long id, String email, String name, String phone) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    public static AuthUserDto from(User user) {
        if (user == null) return null;
        return new AuthUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone()
        );
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
}
