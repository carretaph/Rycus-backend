package com.rycus.Rycus_backend.customer;

public class DuplicateCustomerException extends RuntimeException {

    private final Customer existingCustomer;

    public DuplicateCustomerException(Customer existingCustomer) {
        super("Customer already exists");
        this.existingCustomer = existingCustomer;
    }

    public Customer getExistingCustomer() {
        return existingCustomer;
    }
}
