package com.rycus.Rycus_backend.billing;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.PlanType;
import com.rycus.Rycus_backend.user.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class StripeBillingService {

    private final UserRepository userRepository;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    public StripeBillingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // Verify webhook signature
    // =========================
    public Event verifyWebhook(String payload, String sigHeader)
            throws SignatureVerificationException {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET is missing");
        }
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }

    // =========================
    // Main router
    // =========================
    public void handleEvent(Event event) {

        switch (event.getType()) {

            case "checkout.session.completed" ->
                    handleCheckoutSessionCompleted(event);

            case "customer.subscription.created",
                 "customer.subscription.updated" ->
                    handleSubscriptionUpsert(event);

            case "customer.subscription.deleted" ->
                    handleSubscriptionDeleted(event);

            case "invoice.payment_succeeded" ->
                    handleInvoicePaymentSucceeded(event);

            case "invoice.payment_failed" ->
                    handleInvoicePaymentFailed(event);

            default -> {
                // ignore
            }
        }
    }

    // =========================
    // Handlers
    // =========================

    private void handleCheckoutSessionCompleted(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        if (!(opt.get() instanceof Session session)) return;

        String email = session.getClientReferenceId();
        if (email == null || email.isBlank()) return;

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElse(null);

        if (user == null) return;

        if (session.getCustomer() != null) {
            user.setStripeCustomerId(session.getCustomer());
        }

        if (session.getSubscription() != null) {
            user.setStripeSubscriptionId(session.getSubscription());
        }

        if (user.getPlanType() == null) {
            user.setPlanType(PlanType.FREE_TRIAL);
        }

        userRepository.save(user);
    }

    private void handleSubscriptionUpsert(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        if (!(opt.get() instanceof Subscription sub)) return;

        String customerId = sub.getCustomer();
        if (customerId == null) return;

        User user = userRepository
                .findByStripeCustomerId(customerId)
                .orElse(null);

        if (user == null) return;

        user.setStripeSubscriptionId(sub.getId());
        user.setSubscriptionStatus(sub.getStatus());

        Instant trialEnd = extractEpoch(sub, "trial_end");
        Instant periodEnd = extractEpoch(sub, "current_period_end");

        user.setTrialEndsAt(trialEnd);
        user.setSubscriptionEndsAt(periodEnd);

        userRepository.save(user);
    }

    private void handleSubscriptionDeleted(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        if (!(opt.get() instanceof Subscription sub)) return;

        User user = userRepository
                .findByStripeCustomerId(sub.getCustomer())
                .orElse(null);

        if (user == null) return;

        user.setSubscriptionStatus("canceled");
        userRepository.save(user);
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        if (!(opt.get() instanceof Invoice invoice)) return;

        User user = userRepository
                .findByStripeCustomerId(invoice.getCustomer())
                .orElse(null);

        if (user == null) return;

        user.setSubscriptionStatus("active");
        userRepository.save(user);
    }

    private void handleInvoicePaymentFailed(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        if (!(opt.get() instanceof Invoice invoice)) return;

        User user = userRepository
                .findByStripeCustomerId(invoice.getCustomer())
                .orElse(null);

        if (user == null) return;

        user.setSubscriptionStatus("past_due");
        userRepository.save(user);
    }

    // =========================
    // Helpers
    // =========================
    private Instant extractEpoch(Subscription sub, String field) {
        try {
            if (sub.getRawJsonObject() == null) return null;
            Object v = sub.getRawJsonObject().get(field);
            if (v instanceof Number n) {
                return Instant.ofEpochSecond(n.longValue());
            }
        } catch (Exception ignored) {}
        return null;
    }
}
