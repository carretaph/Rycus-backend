package com.rycus.Rycus_backend.customer;

import com.rycus.Rycus_backend.repository.CustomerRepository;
import com.rycus.Rycus_backend.repository.UserCustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserCustomerRepository userCustomerRepository;

    public CustomerService(CustomerRepository customerRepository,
                           UserCustomerRepository userCustomerRepository) {
        this.customerRepository = customerRepository;
        this.userCustomerRepository = userCustomerRepository;
    }

    // =========================================
    // 1) GLOBAL: todos los customers
    // =========================================
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // =========================================
    // 2) MY CUSTOMERS: lista personal via user_customers
    // =========================================
    public List<Customer> getCustomersForUser(String userEmail) {
        String email = safeTrim(userEmail);
        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User email is required to fetch personal customers"
            );
        }

        return userCustomerRepository
                .findByUserEmailIgnoreCaseOrderByLinkedAtDesc(email)
                .stream()
                .map(UserCustomer::getCustomer)
                .collect(Collectors.toList());
    }

    // =========================================
    // 3) Obtener un customer por ID
    // =========================================
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Customer not found with id " + id
                        )
                );
    }

    // =========================================
    // 4) Crear customer GLOBAL (solo crea) con tu validación de duplicado
    //    (se mantiene para compatibilidad si lo usas en algún lado)
    // =========================================
    public Customer createCustomer(Customer customer) {
        Customer prepared = normalize(customer);
        validateDuplicate(prepared);
        return customerRepository.save(prepared);
    }

    // =========================================
    // 5) Crear o reutilizar customer GLOBAL y linkearlo al usuario
    //    ✅ Esto es el método "bueno" para tu flujo real.
    // =========================================
    @Transactional
    public Customer createOrLinkCustomer(String userEmail, Customer incoming) {
        String email = safeTrim(userEmail);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail is required");
        }

        Customer prepared = normalize(incoming);

        // 1) Intento: si hay email => buscar por email (mejor llave global)
        Customer customer;
        if (prepared.getEmail() != null) {
            customer = customerRepository.findByEmailIgnoreCase(prepared.getEmail())
                    .map(existing -> merge(existing, prepared))
                    .map(customerRepository::save)
                    .orElseGet(() -> customerRepository.save(prepared));
        } else {
            // 2) Si no hay email, aplica tus reglas de duplicado
            Optional<Customer> dup = findDuplicateByYourRules(prepared);
            if (dup.isPresent()) {
                customer = merge(dup.get(), prepared);
                customer = customerRepository.save(customer);
            } else {
                customer = customerRepository.save(prepared);
            }
        }

        // 3) Link a "My Customers"
        linkCustomerToUserById(email, customer.getId());

        return customer;
    }

    // =========================================
    // 6) Link (para usar al dejar reviews)
    //    ✅ Esto cumple: "si deja review, se suma a su lista"
    // =========================================
    @Transactional
    public void linkCustomerToUserById(String userEmail, Long customerId) {
        String email = safeTrim(userEmail);
        if (email == null || customerId == null) return;

        boolean alreadyLinked = userCustomerRepository
                .existsByUserEmailIgnoreCaseAndCustomer_Id(email, customerId);

        if (alreadyLinked) return;

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found with id " + customerId
                ));

        userCustomerRepository.save(new UserCustomer(email, customer));
    }

    // =========================================
    // 7) Actualizar customer (igual que antes)
    // =========================================
    public Customer updateCustomer(Long id, Customer updates) {
        Customer existing = getCustomerById(id);

        if (updates.getFullName() != null) existing.setFullName(updates.getFullName());
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
        if (updates.getAddress() != null) existing.setAddress(updates.getAddress());
        if (updates.getCity() != null) existing.setCity(updates.getCity());
        if (updates.getState() != null) existing.setState(updates.getState());
        if (updates.getZipCode() != null) existing.setZipCode(updates.getZipCode());
        if (updates.getCustomerType() != null) existing.setCustomerType(updates.getCustomerType());
        if (updates.getTags() != null) existing.setTags(updates.getTags());

        return customerRepository.save(existing);
    }

    // =========================================
    // 8) Borrar customer (igual que antes)
    // =========================================
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cannot delete. Customer not found with id " + id
            );
        }
        customerRepository.deleteById(id);
    }

    // =========================================
    // 9) Búsqueda GLOBAL (usa query del repo, no traer todo y filtrar)
    // =========================================
    public List<Customer> searchCustomersGlobal(String term) {
        String normalized = safeTrim(term);
        return customerRepository.searchByText(normalized);
    }

    // =========================================
    // Helpers
    // =========================================
    private Customer normalize(Customer customer) {
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer body is required");
        }

        String fullName = safeTrim(customer.getFullName());
        String email = safeTrim(customer.getEmail());
        String phone = safeTrim(customer.getPhone());

        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);

        return customer;
    }

    private void validateDuplicate(Customer customer) {
        Optional<Customer> dup = findDuplicateByYourRules(customer);
        if (dup.isPresent()) {
            throw new DuplicateCustomerException(dup.get());
        }
    }

    private Optional<Customer> findDuplicateByYourRules(Customer customer) {
        String fullName = customer.getFullName();
        String email = customer.getEmail();
        String phone = customer.getPhone();

        // Regla 1: mismo fullName + mismo phone => duplicado
        if (fullName != null && phone != null) {
            Optional<Customer> existingByNameAndPhone =
                    customerRepository.findByFullNameIgnoreCaseAndPhone(fullName, phone);
            if (existingByNameAndPhone.isPresent()) return existingByNameAndPhone;
        }

        // Regla 2: mismo fullName + mismo email => duplicado
        if (fullName != null && email != null) {
            Optional<Customer> existingByNameAndEmail =
                    customerRepository.findByFullNameIgnoreCaseAndEmail(fullName, email);
            if (existingByNameAndEmail.isPresent()) return existingByNameAndEmail;
        }

        return Optional.empty();
    }

    private Customer merge(Customer existing, Customer incoming) {
        // Solo rellena si viene con algo útil
        if (incoming.getFullName() != null) existing.setFullName(incoming.getFullName());
        if (incoming.getEmail() != null) existing.setEmail(incoming.getEmail());
        if (incoming.getPhone() != null) existing.setPhone(incoming.getPhone());
        if (incoming.getAddress() != null) existing.setAddress(incoming.getAddress());
        if (incoming.getCity() != null) existing.setCity(incoming.getCity());
        if (incoming.getState() != null) existing.setState(incoming.getState());
        if (incoming.getZipCode() != null) existing.setZipCode(incoming.getZipCode());
        if (incoming.getCustomerType() != null) existing.setCustomerType(incoming.getCustomerType());
        if (incoming.getTags() != null) existing.setTags(incoming.getTags());
        return existing;
    }

    private String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
