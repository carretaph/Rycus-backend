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
    }

    // getters/setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
}
