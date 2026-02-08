package com.rycus.Rycus_backend.user;

public class AuthRequest {

    // Compatibilidad: algunas pantallas mandan "name", otras "fullName"
    private String name;
    private String fullName;

    private String email;
    private String password;

    // NEW
    private String phone;

    public AuthRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEffectiveName() {
        if (name != null && !name.trim().isEmpty()) return name.trim();
        if (fullName != null && !fullName.trim().isEmpty()) return fullName.trim();
        return null;
    }

    // ✅ Si algún día necesitas el email tal como llegó
    public String getEmailRaw() {
        return email;
    }

    // ✅ NORMALIZADO: trim + lowercase
    public String getEmail() {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ✅ (opcional) evita errores por espacios al copiar/pegar
    public String getPassword() {
        if (password == null) return null;
        return password.trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
