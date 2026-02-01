package com.rycus.Rycus_backend.milestone;

import com.rycus.Rycus_backend.repository.ReviewRepository;
import com.rycus.Rycus_backend.repository.UserMilestoneRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class MilestoneService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final UserMilestoneRepository milestoneRepository;

    // ✅ Regla final: máximo 3 meses gratis por reviews (10/20/30)
    private static final int MAX_REVIEW_REWARDS = 3;
    private static final long SECONDS_PER_30DAY_MONTH = 30L * 24L * 60L * 60L;

    public MilestoneService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            UserMilestoneRepository milestoneRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.milestoneRepository = milestoneRepository;
    }

    /**
     * ✅ Regla FINAL:
     * - 1 punto por CUSTOMER DISTINTO con al menos 1 review del usuario
     * - Cuenta aunque el customer lo haya creado otra persona
     * - Promo válida solo durante los primeros 3 meses desde el PRIMER REVIEW del usuario
     * - Awards acumulable: 10->1, 20->2, 30->3
     * - Máximo 3 meses gratis por reviews
     *
     * 🎁 Cada award adicional = +1 free month balance
     * ⏳ Y además: extiende accessEndsAt (fuente de verdad de acceso)
     */
    public void evaluateTenCustomerMilestone(Long userId, String userEmail) {

        if (userEmail == null || userEmail.isBlank()) return;

        PromoWindow window = promoWindowFromFirstReview(userEmail);
        if (window == null) return; // sin reviews => nada que evaluar

        // promo expirada => no otorgar nuevos rewards
        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(window.endAt)) return;

        int qualifiedCustomers = reviewRepository.countDistinctCustomersReviewedByUserInWindow(
                userEmail,
                window.startAt,
                window.endAt
        );

        if (qualifiedCustomers < 10) return;

        UserMilestone milestone =
                milestoneRepository
                        .findByUserEmailAndMilestoneType(
                                userEmail,
                                MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW
                        )
                        .orElseGet(() -> {
                            UserMilestone m = new UserMilestone();
                            m.setUserEmail(userEmail);
                            m.setMilestoneType(MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW);
                            m.setTimesAwarded(0);
                            return m;
                        });

        int before = milestone.getTimesAwarded();

        // ✅ awards teóricos: 10->1, 20->2, 30->3...
        int computedAwards = qualifiedCustomers / 10;

        // ✅ cap máximo 3 meses por reviews
        int shouldHaveAwards = Math.min(computedAwards, MAX_REVIEW_REWARDS);

        if (shouldHaveAwards > before) {

            int delta = shouldHaveAwards - before;

            milestone.setTimesAwarded(shouldHaveAwards);
            milestone.setLastAwardedAt(OffsetDateTime.now(ZoneOffset.UTC));
            milestoneRepository.save(milestone);

            // ✅ SUMAR MESES GRATIS REALES + EXTENDER accessEndsAt
            User user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);
            if (user != null && delta > 0) {

                // 1) sumar balance
                user.setFreeMonthsBalance(user.getFreeMonthsBalance() + delta);

                // 2) extender accessEndsAt (fuente de verdad)
                Instant base = computeAccessBase(user);
                Instant extended = base.plusSeconds(SECONDS_PER_30DAY_MONTH * delta);
                user.setAccessEndsAt(extended);

                userRepository.save(user);

                System.out.println("🎁 Free month(s) added for " + userEmail + " (+" + delta + ")");
                System.out.println("⏳ accessEndsAt extended to " + extended);
            }

            System.out.println("🎉 Milestone achieved for user " + userEmail +
                    " (awards: " + shouldHaveAwards + ")");
        }
    }

    // =========================================================
    // ✅ PROGRESO (7/10, 8/10, etc.)
    // =========================================================
    public MilestoneProgressDto getTenCustomerMilestoneProgress(Long userId, String userEmail) {

        if (userEmail == null || userEmail.isBlank()) {
            return MilestoneProgressDto.unauthenticated();
        }

        PromoWindow window = promoWindowFromFirstReview(userEmail);

        // Si todavía no tiene reviews, progreso 0/10
        if (window == null) {
            return new MilestoneProgressDto(
                    MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW.name(),
                    0,
                    0,
                    10,
                    10
            );
        }

        int qualified = reviewRepository.countDistinctCustomersReviewedByUserInWindow(
                userEmail,
                window.startAt,
                window.endAt
        );

        UserMilestone milestone = milestoneRepository
                .findByUserEmailAndMilestoneType(userEmail, MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW)
                .orElse(null);

        int timesAwarded = (milestone == null) ? 0 : milestone.getTimesAwarded();

        // ✅ cap awards (3)
        if (timesAwarded > MAX_REVIEW_REWARDS) timesAwarded = MAX_REVIEW_REWARDS;

        // si ya llegó al máximo, el “next reward” se congela
        if (timesAwarded >= MAX_REVIEW_REWARDS) {
            return new MilestoneProgressDto(
                    MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW.name(),
                    qualified,
                    timesAwarded,
                    30,
                    0
            );
        }

        int nextRewardAt = ((qualified / 10) + 1) * 10;
        if (qualified > 0 && qualified % 10 == 0) nextRewardAt = qualified + 10;

        // pero no más allá de 30
        nextRewardAt = Math.min(nextRewardAt, 30);

        int remaining = Math.max(0, nextRewardAt - qualified);

        return new MilestoneProgressDto(
                MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW.name(),
                qualified,
                timesAwarded,
                nextRewardAt,
                remaining
        );
    }

    // =========================================================
    // Helpers
    // =========================================================

    /**
     * Base para extender acceso:
     * - si accessEndsAt ya existe y es mayor, lo respetamos
     * - si no, usamos subscriptionEndsAt / trialEndsAt si existen
     * - si no, usamos ahora
     */
    private Instant computeAccessBase(User user) {
        Instant now = Instant.now();

        Instant base = user.getAccessEndsAt();

        Instant subEnd = user.getSubscriptionEndsAt();
        Instant trialEnd = user.getTrialEndsAt();

        // toma el mayor entre accessEndsAt, subscriptionEndsAt, trialEndsAt, now
        base = maxInstant(base, subEnd);
        base = maxInstant(base, trialEnd);
        base = maxInstant(base, now);

        return base;
    }

    private Instant maxInstant(Instant a, Instant b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private PromoWindow promoWindowFromFirstReview(String userEmail) {
        LocalDateTime firstReviewAt = reviewRepository
                .findFirstReviewAtByUser(userEmail)
                .orElse(null);

        if (firstReviewAt == null) return null;

        LocalDateTime startAt = firstReviewAt;
        LocalDateTime endAt = startAt.plusMonths(3);

        return new PromoWindow(startAt, endAt);
    }

    private static class PromoWindow {
        final LocalDateTime startAt;
        final LocalDateTime endAt;

        PromoWindow(LocalDateTime startAt, LocalDateTime endAt) {
            this.startAt = startAt;
            this.endAt = endAt;
        }
    }
}
