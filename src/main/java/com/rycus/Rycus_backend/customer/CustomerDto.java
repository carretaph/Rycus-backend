package com.rycus.Rycus_backend.customer;

public class CustomerDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String customerType;

    // NUEVO
    private Double latitude;
    private Double longitude;

    public CustomerDto() {}

    public CustomerDto(Customer c) {
        this.id = c.getId();
        this.fullName = c.getFullName();
        this.email = c.getEmail();
        this.phone = c.getPhone();
        this.address = c.getAddress();
        this.city = c.getCity();
        this.state = c.getState();
        this.zipCode = c.getZipCode();
        this.customerType = c.getCustomerType();

        // NUEVO
        this.latitude = c.getLatitude();
        this.longitude = c.getLongitude();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getCustomerType() { return customerType; }

    // NUEVO
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}