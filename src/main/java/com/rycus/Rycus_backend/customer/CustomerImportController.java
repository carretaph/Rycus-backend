package com.rycus.Rycus_backend.customer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin
public class CustomerImportController {

    private final CustomerService customerService;

    public CustomerImportController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importCustomers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userEmail") String userEmail
    ) {
        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            boolean firstRow = true;

            while ((line = reader.readLine()) != null) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                List<String> cols = parseCsvLine(line);

                // A = number, B = customer, C = address, D = city, E = state, F = zip, G = phone, H = email
                if (cols.size() < 8) {
                    skipped++;
                    continue;
                }

                Customer customer = new Customer();
                customer.setFullName(clean(cols.get(1)));
                customer.setAddress(clean(cols.get(2)));
                customer.setCity(clean(cols.get(3)));
                customer.setState(clean(cols.get(4)));
                customer.setZipCode(clean(cols.get(5)));
                customer.setPhone(clean(cols.get(6)));
                customer.setEmail(clean(cols.get(7)));

                if (customer.getFullName() == null || customer.getFullName().isBlank()) {
                    skipped++;
                    continue;
                }

                customerService.createOrLinkCustomer(userEmail, customer);
                imported++;
            }

            return ResponseEntity.ok("Imported customers: " + imported + " | Skipped: " + skipped);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Import failed: " + ex.getMessage());
        }
    }

    private static String clean(String value) {
        if (value == null) return null;

        String cleaned = value
                .replace("\uFEFF", "")
                .replace("\"", "")
                .trim();

        return cleaned.isBlank() ? null : cleaned;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(current.toString());
        return result;
    }
}