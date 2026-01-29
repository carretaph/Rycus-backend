package com.rycus.Rycus_backend.billing;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/billing")
public class BillingWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        switch (event.getType()) {

            case "checkout.session.completed" -> {
                System.out.println("✅ Checkout completed");
            }

            case "customer.subscription.created" -> {
                System.out.println("🟢 Subscription created");
            }

            case "customer.subscription.updated" -> {
                System.out.println("🟡 Subscription updated");
            }

            case "customer.subscription.deleted" -> {
                System.out.println("🔴 Subscription canceled");
            }

            default -> {
                System.out.println("ℹ️ Event ignored: " + event.getType());
            }
        }

        return ResponseEntity.ok("received");
    }
}
