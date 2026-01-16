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
     * - Promo válida solo durante los primeros 3 meses desde users.created_at
     * - Awards acumulable: 10->1, 20->2...
     */
    public void evaluateTenCustomerMilestone(Long userId, String userEmail) {

        if (userEmail == null || userEmail.isBlank()) return;

        User user = resolveUser(userId, userEmail);
        if (user == null || user.getCreatedAt() == null) return;

        PromoWindow window = promoWindowFromUserCreatedAt(user.getCreatedAt());

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
                            return m;
                        });

        int shouldHaveAwards = qualifiedCustomers / 10;

        if (shouldHaveAwards > milestone.getTimesAwarded()) {
            milestone.setTimesAwarded(shouldHaveAwards);
            milestone.setLastAwardedAt(OffsetDateTime.now());
            milestoneRepository.save(milestone);

            System.out.println("🎉 Milestone achieved for user " + userEmail +
                    " (awards: " + shouldHaveAwards + ")");
        }
    }

    // =========================================================
    // ✅ PROGRESO (7/10, 8/10, etc.)
    // =========================================================
    public MilestoneProgressDto getTenCustomerMilestoneProgress(Long userId, String userEmail) {

        if (userEmail == null || userEmail.isBlank()) {
            return MilestoneProgressDto.empty();
        }

        User user = resolveUser(userId, userEmail);
        if (user == null || user.getCreatedAt() == null) {
            return MilestoneProgressDto.empty();
        }

        PromoWindow window = promoWindowFromUserCreatedAt(user.getCreatedAt());

        int qualified = reviewRepository.countDistinctCustomersReviewedByUserInWindow(
                userEmail,
                window.startAt,
                window.endAt
        );

        UserMilestone milestone = milestoneRepository
                .findByUserEmailAndMilestoneType(userEmail, MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW)
                .orElse(null);

        int timesAwarded = (milestone == null) ? 0 : milestone.getTimesAwarded();

        int nextRewardAt = ((qualified / 10) + 1) * 10;
        if (qualified > 0 && qualified % 10 == 0) nextRewardAt = qualified + 10;

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
    private User resolveUser(Long userId, String userEmail) {
        User user = null;

        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null) {
            user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);
        }
        return user;
    }

    private PromoWindow promoWindowFromUserCreatedAt(Instant createdAt) {
        LocalDateTime startAt = LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC);
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
