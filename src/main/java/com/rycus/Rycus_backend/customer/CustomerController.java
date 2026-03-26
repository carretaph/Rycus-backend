package com.rycus.Rycus_backend.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // =========================================
    // GET /customers
    // =========================================
    @GetMapping
    public ResponseEntity<List<CustomerDto>> getCustomers(
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        try {
            List<Customer> customers;

            if (userEmail != null && !userEmail.isBlank()) {
                String normalized = userEmail.trim().toLowerCase();
                customers = customerService.getCustomersForUser(normalized);
            } else {
                customers = customerService.getAllCustomers();
            }

            List<CustomerDto> dtos = customers.stream().map(CustomerDto::new).toList();
            return ResponseEntity.ok(dtos);

        } catch (Exception ex) {
            System.out.println("❌ GET /customers failed. userEmail=" + userEmail);
            ex.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // =========================================
    // 🔍 SEARCH
    // =========================================
    @GetMapping("/search")
    public ResponseEntity<List<CustomerDto>> searchCustomers(@RequestParam("q") String query) {
        try {
            String q = (query == null) ? "" : query.trim();
            List<Customer> results = customerService.searchCustomersGlobal(q);

            List<CustomerDto> dtos = results.stream().map(CustomerDto::new).toList();
            return ResponseEntity.ok(dtos);

        } catch (Exception ex) {
            System.out.println("❌ GET /customers/search failed. q=" + query);
            ex.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // =========================================
    // GET BY ID
    // =========================================
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        try {
            Customer c = customerService.getCustomerById(id);
            return ResponseEntity.ok(new CustomerDto(c));

        } catch (Exception ex) {
            System.out.println("❌ GET /customers/" + id + " failed.");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // =========================================
    // CREATE
    // =========================================
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(
            @RequestBody Customer customer,
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        try {
            Customer result;

            if (userEmail != null && !userEmail.isBlank()) {
                String normalized = userEmail.trim().toLowerCase();
                result = customerService.createOrLinkCustomer(normalized, customer);
            } else {
                result = customerService.createCustomer(customer);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new CustomerDto(result));

        } catch (Exception ex) {
            System.out.println("❌ POST /customers failed. userEmail=" + userEmail);
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // LINK
    // =========================================
    @PostMapping("/{id}/link")
    public ResponseEntity<Void> linkCustomerToUser(
            @PathVariable Long id,
            @RequestParam("userEmail") String userEmail
    ) {
        try {
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            String normalized = userEmail.trim().toLowerCase();
            customerService.linkCustomerToUserById(normalized, id);

            return ResponseEntity.noContent().build();

        } catch (Exception ex) {
            System.out.println("❌ POST /customers/" + id + "/link failed. userEmail=" + userEmail);
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // UPDATE
    // =========================================
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer updates
    ) {
        try {
            Customer updated = customerService.updateCustomer(id, updates);
            return ResponseEntity.ok(new CustomerDto(updated));

        } catch (Exception ex) {
            System.out.println("❌ PUT /customers/" + id + " failed.");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // DELETE
    // =========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();

        } catch (Exception ex) {
            System.out.println("❌ DELETE /customers/" + id + " failed.");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================
    // 🔥 NEW: GEOCODE ALL (BACKFILL)
    // =========================================
    @PostMapping("/geocode-all")
    public ResponseEntity<String> geocodeAllCustomers() {
        try {
            int updated = customerService.geocodeAllMissingCustomers();
            return ResponseEntity.ok("Geocoded customers updated: " + updated);

        } catch (Exception ex) {
            System.out.println("❌ POST /customers/geocode-all failed.");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error running geocode-all");
        }
    }

    // =========================================
    // 🔥 NEW: GEOCODE ONE
    // =========================================
    @PostMapping("/{id}/geocode")
    public ResponseEntity<String> geocodeCustomer(@PathVariable Long id) {
        try {
            int updated = customerService.geocodeCustomerById(id);
            return ResponseEntity.ok("Geocoded customer updated: " + updated);

        } catch (Exception ex) {
            System.out.println("❌ POST /customers/" + id + "/geocode failed.");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error geocoding customer");
        }
    }
}