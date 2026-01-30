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
    public Event verifyWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET is missing");
        }
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }

    // =========================
    // Main event router
    // =========================
    public void handleEvent(Event event) {
        switch (event.getType()) {

            // When checkout completes: we capture customer/subscription IDs
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);

            // Subscription lifecycle
            case "customer.subscription.created" -> handleSubscriptionUpsert(event);
            case "customer.subscription.updated" -> handleSubscriptionUpsert(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);

            // Payment lifecycle
            case "invoice.payment_succeeded" -> handleInvoicePaymentSucceeded(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);

            default -> {
                // ignore
            }
        }
    }

    // =========================
    // Handlers
    // =========================

    /**
     * Captura stripeCustomerId + stripeSubscriptionId usando el email que guardaste en client_reference_id.
     */
    private void handleCheckoutSessionCompleted(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        StripeObject obj = opt.get();
        if (!(obj instanceof Session session)) return;

        String email = session.getClientReferenceId(); // lo seteaste como email
        if (email == null || email.isBlank()) return;

        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null) return;

        if (session.getCustomer() != null) {
            user.setStripeCustomerId(session.getCustomer());
        }

        if (session.getSubscription() != null) {
            user.setStripeSubscriptionId(session.getSubscription());
        }

        // No inventamos PRO aquí (tu PlanType no necesariamente lo tiene).
        // El acceso lo define subscriptionStatus + accessEndsAt.
        if (user.getPlanType() == null) {
            user.setPlanType(PlanType.FREE_TRIAL);
        }

        userRepository.save(user);
    }

    /**
     * created/updated: actualiza status y fechas.
     * accessEndsAt = max(currentPeriodEnd (o trialEnd), accessEndsAt existente extendida por rewards)
     */
    private void handleSubscriptionUpsert(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        StripeObject obj = opt.get();
        if (!(obj instanceof Subscription sub)) return;

        String customerId = sub.getCustomer();
        if (customerId == null || customerId.isBlank()) return;

        User user = userRepository.findByStripeCustomerId(customerId).orElse(null);
        if (user == null) return;

        user.setStripeSubscriptionId(sub.getId());
        user.setSubscriptionStatus(sub.getStatus()); // trialing, active, past_due, canceled, unpaid...

        Instant trialEnd = (sub.getTrialEnd() == null) ? null : Instant.ofEpochSecond(sub.getTrialEnd());
        Instant periodEnd = (sub.getCurrentPeriodEnd() == null) ? null : Instant.ofEpochSecond(sub.getCurrentPeriodEnd());

        user.setTrialEndsAt(trialEnd);
        user.setSubscriptionEndsAt(periodEnd);

        Instant computedAccess = computeAccessEndsAt(user, trialEnd, periodEnd);
        user.setAccessEndsAt(computedAccess);

        userRepository.save(user);
    }

    /**
     * deleted: marca canceled pero NO recorta accessEndsAt.
     */
    private void handleSubscriptionDeleted(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        StripeObject obj = opt.get();
        if (!(obj instanceof Subscription sub)) return;

        String customerId = sub.getCustomer();
        if (customerId == null || customerId.isBlank()) return;

        User user = userRepository.findByStripeCustomerId(customerId).orElse(null);
        if (user == null) return;

        user.setSubscriptionStatus("canceled");
        // accessEndsAt queda igual (respeta lo ya pagado o meses gratis ganados)
        userRepository.save(user);
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        StripeObject obj = opt.get();
        if (!(obj instanceof Invoice invoice)) return;

        String customerId = invoice.getCustomer();
        if (customerId == null || customerId.isBlank()) return;

        User user = userRepository.findByStripeCustomerId(customerId).orElse(null);
        if (user == null) return;

        user.setSubscriptionStatus("active");
        userRepository.save(user);
    }

    private void handleInvoicePaymentFailed(Event event) {
        Optional<StripeObject> opt = event.getDataObjectDeserializer().getObject();
        if (opt.isEmpty()) return;

        StripeObject obj = opt.get();
        if (!(obj instanceof Invoice invoice)) return;

        String customerId = invoice.getCustomer();
        if (customerId == null || customerId.isBlank()) return;

        User user = userRepository.findByStripeCustomerId(customerId).orElse(null);
        if (user == null) return;

        user.setSubscriptionStatus("past_due");
        // NO tocamos accessEndsAt aquí
        userRepository.save(user);
    }

    // =========================
    // Access computation
    // =========================
    private Instant computeAccessEndsAt(User user, Instant trialEnd, Instant periodEnd) {
        Instant base = periodEnd;

        // Si no hay periodEnd, usa trialEnd como base
        if (base == null) base = trialEnd;

        // Si tampoco hay trialEnd, usa ahora
        if (base == null) base = Instant.now();

        // Nunca le recortes si ya tenía un accessEndsAt mayor
        if (user.getAccessEndsAt() != null && user.getAccessEndsAt().isAfter(base)) {
            base = user.getAccessEndsAt();
        }

        // Rewards: extiende por freeMonthsBalance
        int free = Math.max(0, user.getFreeMonthsBalance());
        if (free <= 0) return base;

        return base.plusSeconds(30L * 24L * 60L * 60L * free); // 30 días por mes (simple y estable)
    }
}
