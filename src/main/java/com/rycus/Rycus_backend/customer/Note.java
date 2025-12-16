package com.rycus.Rycus_backend.customer;

public class Note {

    private Long id;
    private Long customerId;
    private String type;   // GENERAL or CORRECTION
    private String message;

    public Note() {
    }

    public Note(Long id, Long customerId, String type, String message) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
