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

    @Value("${GOOGLE_GEOCODING_API_KEY:${GOOGLE_MAPS_API_KEY:}}")
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

    @Transactional
    public int geocodeAllMissingCustomers() {
        List<Customer> customers = customerRepository.findAll();
        int updated = 0;

        for (Customer customer : customers) {
            boolean missingCoords =
                    customer.getLatitude() == null || customer.getLongitude() == null;

            if (!missingCoords) {
                continue;
            }

            Double beforeLat = customer.getLatitude();
            Double beforeLng = customer.getLongitude();

            geocodeIfPossible(customer);

            boolean nowHasCoords =
                    customer.getLatitude() != null && customer.getLongitude() != null;

            boolean changed =
                    (beforeLat == null && beforeLng == null && nowHasCoords) ||
                            (beforeLat != null && beforeLng != null &&
                                    (!beforeLat.equals(customer.getLatitude()) || !beforeLng.equals(customer.getLongitude())));

            if (changed) {
                customerRepository.save(customer);
                updated++;
            }
        }

        return updated;
    }

    @Transactional
    public int geocodeCustomerById(Long customerId) {
        Customer customer = getCustomerById(customerId);

        Double beforeLat = customer.getLatitude();
        Double beforeLng = customer.getLongitude();

        geocodeIfPossible(customer);

        boolean nowHasCoords =
                customer.getLatitude() != null && customer.getLongitude() != null;

        boolean changed =
                (beforeLat == null && beforeLng == null && nowHasCoords) ||
                        (beforeLat != null && beforeLng != null &&
                                (!beforeLat.equals(customer.getLatitude()) || !beforeLng.equals(customer.getLongitude())));

        if (changed) {
            customerRepository.save(customer);
            return 1;
        }

        return 0;
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer body required");
        }

        customer.setFullName(safeTrim(customer.getFullName()));
        customer.setEmail(safeTrim(customer.getEmail()));
        customer.setPhone(safeTrim(customer.getPhone()));
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
            Optional<Customer> existing =
                    customerRepository.findByFullNameIgnoreCaseAndPhone(fullName, phone);
            if (existing.isPresent()) return existing;
        }

        if (fullName != null && email != null) {
            Optional<Customer> existing =
                    customerRepository.findByFullNameIgnoreCaseAndEmail(fullName, email);
            if (existing.isPresent()) return existing;
        }

        return Optional.empty();
    }

    private Customer merge(Customer existing, Customer incoming) {
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

    private void geocodeIfPossible(Customer customer) {
        String address = buildFullAddress(customer);
        if (address == null) {
            return;
        }

        String apiKey = googleMapsApiKey == null ? "" : googleMapsApiKey.trim();

        if (apiKey.isBlank()) {
            System.out.println("⚠️ GOOGLE_GEOCODING_API_KEY missing in backend.");
            return;
        }

        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(address, StandardCharsets.UTF_8)
                    + "&key="
                    + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            String status = root.path("status").asText();
            String errorMessage = root.path("error_message").asText("");

            if (!"OK".equals(status)) {
                System.out.println("⚠️ Geocoding failed: " + status + " for " + address);
                if (!errorMessage.isBlank()) {
                    System.out.println("⚠️ Google error_message: " + errorMessage);
                }
                return;
            }

            JsonNode location = root.path("results")
                    .get(0)
                    .path("geometry")
                    .path("location");

            customer.setLatitude(location.path("lat").asDouble());
            customer.setLongitude(location.path("lng").asDouble());

            System.out.println("✅ Geocoded: " + address);

        } catch (Exception ex) {
            System.out.println("⚠️ Geocoding exception for " + address);
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