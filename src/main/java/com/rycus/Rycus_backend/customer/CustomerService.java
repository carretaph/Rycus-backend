package com.rycus.Rycus_backend.customer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rycus.Rycus_backend.repository.CustomerRepository;
import com.rycus.Rycus_backend.repository.UserCustomerRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserCustomerRepository userCustomerRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${GOOGLE_MAPS_API_KEY:}")
    private String googleMapsApiKey;

    public CustomerService(CustomerRepository customerRepository,
                           UserCustomerRepository userCustomerRepository,
                           UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userCustomerRepository = userCustomerRepository;
        this.userRepository = userRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> getCustomersForUser(String userEmail) {
        String email = safeTrim(userEmail);

        if (email == null) {
            return List.of();
        }

        try {
            return customerRepository.findCustomersLinkedToUser(email);
        } catch (Exception ex) {
            ex.printStackTrace();
            return List.of();
        }
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Customer not found with id " + id
                        )
                );
    }

    public Customer createCustomer(Customer customer) {
        Customer prepared = normalize(customer);
        validateDuplicate(prepared);
        geocodeIfPossible(prepared);

        return customerRepository.save(prepared);
    }

    @Transactional
    public Customer createOrLinkCustomer(String userEmail, Customer incoming) {
        String email = safeTrim(userEmail);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail is required");
        }

        Customer prepared = normalize(incoming);
        Long creatorUserId = getUserIdByEmailOrNull(email);

        Customer customer;

        if (prepared.getEmail() != null) {
            customer = customerRepository.findByEmailIgnoreCase(prepared.getEmail())
                    .map(existing -> merge(existing, prepared))
                    .map(c -> {
                        geocodeIfPossible(c);
                        return customerRepository.save(c);
                    })
                    .orElseGet(() -> {
                        prepared.setCreatedByUserId(creatorUserId);
                        geocodeIfPossible(prepared);
                        return customerRepository.save(prepared);
                    });

        } else {
            Optional<Customer> dup = findDuplicateByYourRules(prepared);

            if (dup.isPresent()) {
                customer = merge(dup.get(), prepared);
                geocodeIfPossible(customer);
                customer = customerRepository.save(customer);

            } else {
                prepared.setCreatedByUserId(creatorUserId);
                geocodeIfPossible(prepared);
                customer = customerRepository.save(prepared);
            }
        }

        linkCustomerToUserById(email, customer.getId());
        return customer;
    }

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

    public Customer updateCustomer(Long id, Customer updates) {
        Customer existing = getCustomerById(id);

        boolean addressChanged = false;

        if (updates.getFullName() != null) existing.setFullName(updates.getFullName());
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getPhone() != null) existing.setPhone(updates.getPhone());

        if (updates.getAddress() != null) {
            existing.setAddress(updates.getAddress());
            addressChanged = true;
        }
        if (updates.getCity() != null) {
            existing.setCity(updates.getCity());
            addressChanged = true;
        }
        if (updates.getState() != null) {
            existing.setState(updates.getState());
            addressChanged = true;
        }
        if (updates.getZipCode() != null) {
            existing.setZipCode(updates.getZipCode());
            addressChanged = true;
        }

        if (updates.getCustomerType() != null) existing.setCustomerType(updates.getCustomerType());
        if (updates.getTags() != null) existing.setTags(updates.getTags());

        if (addressChanged) {
            geocodeIfPossible(existing);
        }

        return customerRepository.save(existing);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cannot delete. Customer not found with id " + id
            );
        }
        customerRepository.deleteById(id);
    }

    public List<Customer> searchCustomersGlobal(String term) {
        String normalized = safeTrim(term);
        return customerRepository.searchByText(normalized);
    }

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

        customer.setAddress(safeTrim(customer.getAddress()));
        customer.setCity(safeTrim(customer.getCity()));
        customer.setState(safeTrim(customer.getState()));
        customer.setZipCode(safeTrim(customer.getZipCode()));

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

        if (fullName != null && phone != null) {
            Optional<Customer> existingByNameAndPhone =
                    customerRepository.findByFullNameIgnoreCaseAndPhone(fullName, phone);
            if (existingByNameAndPhone.isPresent()) return existingByNameAndPhone;
        }

        if (fullName != null && email != null) {
            Optional<Customer> existingByNameAndEmail =
                    customerRepository.findByFullNameIgnoreCaseAndEmail(fullName, email);
            if (existingByNameAndEmail.isPresent()) return existingByNameAndEmail;
        }

        return Optional.empty();
    }

    private Customer merge(Customer existing, Customer incoming) {
        boolean addressChanged = false;

        if (incoming.getFullName() != null) existing.setFullName(incoming.getFullName());
        if (incoming.getEmail() != null) existing.setEmail(incoming.getEmail());
        if (incoming.getPhone() != null) existing.setPhone(incoming.getPhone());

        if (incoming.getAddress() != null) {
            existing.setAddress(incoming.getAddress());
            addressChanged = true;
        }
        if (incoming.getCity() != null) {
            existing.setCity(incoming.getCity());
            addressChanged = true;
        }
        if (incoming.getState() != null) {
            existing.setState(incoming.getState());
            addressChanged = true;
        }
        if (incoming.getZipCode() != null) {
            existing.setZipCode(incoming.getZipCode());
            addressChanged = true;
        }

        if (incoming.getCustomerType() != null) existing.setCustomerType(incoming.getCustomerType());
        if (incoming.getTags() != null) existing.setTags(incoming.getTags());

        if (addressChanged) {
            geocodeIfPossible(existing);
        }

        return existing;
    }

    private void geocodeIfPossible(Customer customer) {
        String address = buildFullAddress(customer);
        if (address == null) {
            return;
        }

        if (googleMapsApiKey == null || googleMapsApiKey.isBlank()) {
            System.out.println("⚠️ GOOGLE_MAPS_API_KEY missing in backend. Skipping geocoding.");
            return;
        }

        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(address, StandardCharsets.UTF_8)
                    + "&key="
                    + URLEncoder.encode(googleMapsApiKey, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("⚠️ Geocoding HTTP error: " + response.statusCode() + " for " + address);
                return;
            }

            JsonNode root = objectMapper.readTree(response.body());
            String status = root.path("status").asText();

            if (!"OK".equals(status)) {
                System.out.println("⚠️ Geocoding failed: " + status + " for " + address);
                return;
            }

            JsonNode location = root.path("results").get(0).path("geometry").path("location");
            if (location.isMissingNode()) {
                return;
            }

            customer.setLatitude(location.path("lat").asDouble());
            customer.setLongitude(location.path("lng").asDouble());

        } catch (Exception ex) {
            System.out.println("⚠️ Geocoding exception for address: " + address);
            ex.printStackTrace();
        }
    }

    private String buildFullAddress(Customer customer) {
        String address = safeTrim(customer.getAddress());
        String city = safeTrim(customer.getCity());
        String state = safeTrim(customer.getState());
        String zip = safeTrim(customer.getZipCode());

        String full = String.join(" ",
                address == null ? "" : address,
                city == null ? "" : city,
                state == null ? "" : state,
                zip == null ? "" : zip
        ).trim();

        return full.isBlank() ? null : full;
    }

    private String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long getUserIdByEmailOrNull(String userEmail) {
        String e = safeTrim(userEmail);
        if (e == null) return null;

        String normalized = e.toLowerCase(Locale.ROOT);

        return userRepository.findByEmailIgnoreCase(normalized)
                .map(User::getId)
                .orElse(null);
    }
}