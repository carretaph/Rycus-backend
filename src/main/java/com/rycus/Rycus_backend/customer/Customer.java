package com.rycus.Rycus_backend.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customers_email", columnList = "email"),
                @Index(name = "idx_customers_fullName", columnList = "fullName"),
                @Index(name = "idx_customers_phone", columnList = "phone"),
                @Index(name = "idx_customers_zip", columnList = "zipCode"),

                // ✅ nuevos indexes recomendados
                @Index(name = "idx_customers_createdBy", columnList = "createdByUserId"),
                @Index(name = "idx_customers_createdAt", columnList = "createdAt")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_email", columnNames = {"email"})
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String fullName;

    @Column(length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 180)
    private String address;

    @Column(length = 80)
    private String city;

    @Column(length = 30)
    private String state;

    @Column(length = 20)
    private String zipCode;

    @Column(length = 30)
    private String customerType;

    @Column(length = 300)
    private String tags;

    // ✅ NUEVO: quien creó este customer “global”
    @Column(nullable = true)
    private Long createdByUserId;

    // ✅ NUEVO: cuándo fue creado
    @Column(nullable = true)
    private Instant createdAt;

    public Customer() {}

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

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

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
