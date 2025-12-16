package com.rycus.Rycus_backend.user;

public class AuthResponse {

    private String message;
    private AuthUserDto user;

    public AuthResponse() {}

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String message, AuthUserDto user) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public AuthUserDto getUser() {
        return user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(AuthUserDto user) {
        this.user = user;
    }
}
