package com.rycus.Rycus_backend.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // =========================================
    // GET /customers
    // Si viene userEmail => devuelve SOLO sus clientes (My Customers)
    // Si no viene => devuelve todos (Global)
    // =========================================
    @GetMapping
    public List<Customer> getCustomers(
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        if (userEmail != null && !userEmail.isBlank()) {
            return customerService.getCustomersForUser(userEmail);
        }
        return customerService.getAllCustomers();
    }

    // =========================================
    // 🔍 GET /customers/search?q=texto
    // Búsqueda GLOBAL
    // =========================================
    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam("q") String query) {
        List<Customer> results = customerService.searchCustomersGlobal(query);
        return ResponseEntity.ok(results);
    }

    // =========================================
    // GET /customers/{id}
    // =========================================
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    // =========================================
    // POST /customers
    // - Si viene userEmail => crea o reutiliza customer GLOBAL y lo linkea a My Customers
    // - Si no viene userEmail => crea customer GLOBAL (compatibilidad)
    // =========================================
    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @RequestBody Customer customer,
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        Customer result;

        if (userEmail != null && !userEmail.isBlank()) {
            result = customerService.createOrLinkCustomer(userEmail, customer);
        } else {
            result = customerService.createCustomer(customer);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // =========================================
    // PUT /customers/{id}
    // =========================================
    @PutMapping("/{id}")
    public Customer updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer updates
    ) {
        return customerService.updateCustomer(id, updates);
    }

    // =========================================
    // DELETE /customers/{id}
    // =========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
