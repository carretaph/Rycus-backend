package com.rycus.Rycus_backend.billing;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing")
public class BillingWebhookController {

    private final StripeBillingService stripeBillingService;

    public BillingWebhookController(StripeBillingService stripeBillingService) {
        this.stripeBillingService = stripeBillingService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature");
        }

        Event event;
        try {
            event = stripeBillingService.verifyWebhook(payload, sigHeader);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Webhook verify failed: " + e.getMessage());
        }

        try {
            stripeBillingService.handleEvent(event);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Webhook handle failed: " + e.getMessage());
        }

        return ResponseEntity.ok("received");
    }
}
