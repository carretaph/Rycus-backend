package com.rycus.Rycus_backend.billing;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/billing")
@CrossOrigin
public class BillingController {

    private final StripeCheckoutService stripeCheckoutService;

    public BillingController(StripeCheckoutService stripeCheckoutService) {
        this.stripeCheckoutService = stripeCheckoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(Authentication authentication) throws Exception {
        String email = (authentication == null) ? null : authentication.getName();
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String url = stripeCheckoutService.createCheckoutUrl(email.trim().toLowerCase());
        return ResponseEntity.ok(Map.of("url", url));
    }
}
