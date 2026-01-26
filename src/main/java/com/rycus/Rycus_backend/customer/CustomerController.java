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
    // Si viene userEmail => devuelve SOLO sus clientes (My Customers)
    // Si no viene => devuelve todos (Global)
    //
    // ✅ Devuelve DTO para evitar LazyInitialization (no Session)
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
    // 🔍 GET /customers/search?q=texto
    // Búsqueda GLOBAL
    //
    // ✅ Devuelve DTO para evitar LazyInitialization
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
    // GET /customers/{id}
    //
    // ✅ Devuelve DTO para evitar LazyInitialization
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
    // POST /customers
    // - Si viene userEmail => crea o reutiliza customer GLOBAL y lo linkea a My Customers
    // - Si no viene userEmail => crea customer GLOBAL (compatibilidad)
    //
    // ✅ Devuelve DTO por consistencia
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
    // POST /customers/{id}/link
    // Linkea un customer EXISTENTE a un usuario (My Customers)
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
    // PUT /customers/{id}
    //
    // ✅ Devuelve DTO para evitar LazyInitialization
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
    // DELETE /customers/{id}
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
}
