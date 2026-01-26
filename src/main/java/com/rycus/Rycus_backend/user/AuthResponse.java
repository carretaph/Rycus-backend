package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.user.dto.SafeUserDto;

public class AuthResponse {

    private String message;
    private String token;
    private SafeUserDto user;

    public AuthResponse() {}

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String message, String token, SafeUserDto user) {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public SafeUserDto getUser() { return user; }
    public void setUser(SafeUserDto user) { this.user = user; }
}
