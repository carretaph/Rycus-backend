package com.rycus.Rycus_backend.billing;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.PlanType;
import com.rycus.Rycus_backend.user.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StripeBillingService {

    private final UserRepository userRepository;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    public StripeBillingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verifica que el webhook venga realmente de Stripe
     */
    public Event verifyWebhook(String payload, String sigHeader)
            throws SignatureVerificationException {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET is missing");
        }

        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }

    /**
     * Router de eventos Stripe
     */
    public void handleEvent(Event event) {
        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            default -> {
                // otros eventos se ignoran por ahora
            }
        }
    }

    /**
     * Se ejecuta cuando el checkout se completa con éxito
     */
    private void handleCheckoutSessionCompleted(Event event) {

        Optional<StripeObject> optionalObject =
                event.getDataObjectDeserializer().getObject();

        if (optionalObject.isEmpty()) return;

        StripeObject stripeObject = optionalObject.get();

        if (!(stripeObject instanceof Session session)) return;

        // 🔑 usamos client_reference_id como email
        String email = session.getClientReferenceId();
        if (email == null || email.isBlank()) return;

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElse(null);

        if (user == null) return;

        // Guardamos IDs de Stripe
        if (session.getCustomer() != null) {
            user.setStripeCustomerId(session.getCustomer());
        }

        if (session.getSubscription() != null) {
            user.setStripeSubscriptionId(session.getSubscription());
        }

        // ⚠️ NO usamos PRO (no existe en tu enum)
        // El acceso real lo controlas por fechas / estado
        user.setPlanType(PlanType.FREE_TRIAL);

        userRepository.save(user);
    }
}
