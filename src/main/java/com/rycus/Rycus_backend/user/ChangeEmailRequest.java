package com.rycus.Rycus_backend.user;

public class ChangeEmailRequest {

    private String currentEmail;
    private String newEmail;
    private String password;

    public ChangeEmailRequest() {}

    public String getCurrentEmail() { return currentEmail; }
    public void setCurrentEmail(String currentEmail) { this.currentEmail = currentEmail; }

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
