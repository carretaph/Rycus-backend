package com.rycus.Rycus_backend.billing;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeCheckoutService {

    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;

    @Value("${stripe.price.id:}")
    private String priceId;

    @Value("${rycus.app.url:https://rycus.app}")
    private String frontendUrl;

    public String createCheckoutUrl(String userEmail) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY is missing");
        }
        if (priceId == null || priceId.isBlank()) {
            throw new IllegalStateException("STRIPE_PRICE_ID is missing");
        }

        Stripe.apiKey = stripeSecretKey;

        String successUrl = frontendUrl + "/billing/success";
        String cancelUrl  = frontendUrl + "/billing/cancel";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(userEmail)
                .setCustomerEmail(userEmail)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .setTrialPeriodDays(30L) // ✅ 30 días trial
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
