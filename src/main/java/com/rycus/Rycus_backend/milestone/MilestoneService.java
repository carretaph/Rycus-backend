package com.rycus.Rycus_backend.milestone;

import com.rycus.Rycus_backend.repository.CustomerRepository;
import com.rycus.Rycus_backend.repository.UserMilestoneRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class MilestoneService {

    private final CustomerRepository customerRepository;
    private final UserMilestoneRepository milestoneRepository;

    public MilestoneService(
            CustomerRepository customerRepository,
            UserMilestoneRepository milestoneRepository
    ) {
        this.customerRepository = customerRepository;
        this.milestoneRepository = milestoneRepository;
    }

    /**
     * Evalúa:
     * Por cada 10 clientes NUEVOS creados por el usuario (createdByUserId)
     * a los que les dejó al menos 1 review (Review.createdBy = userEmail),
     * se otorga 1 reward.
     *
     * NOTA: awards es acumulable:
     * 10 -> 1
     * 20 -> 2
     * 30 -> 3
     */
    public void evaluateTenCustomerMilestone(Long userId, String userEmail) {

        if (userId == null) return;
        if (userEmail == null || userEmail.isBlank()) return;

        int qualifiedCustomers =
                customerRepository.countDistinctCustomersWithReviewByUser(userId, userEmail);

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

            // 🎁 Aquí luego conectamos meses gratis / rewards reales
            System.out.println(
                    "🎉 Milestone achieved for user " + userEmail +
                            " (awards: " + shouldHaveAwards + ")"
            );
        }
    }

    // =========================================================
    // ✅ PUNTO 2: PROGRESO (para mostrar 7/10, 8/10, etc.)
    // =========================================================
    public MilestoneProgressDto getTenCustomerMilestoneProgress(Long userId, String userEmail) {

        if (userId == null || userEmail == null || userEmail.isBlank()) {
            return new MilestoneProgressDto(
                    MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW.name(),
                    0,
                    0,
                    10,
                    10
            );
        }

        int qualified = customerRepository.countDistinctCustomersWithReviewByUser(userId, userEmail);

        UserMilestone milestone = milestoneRepository
                .findByUserEmailAndMilestoneType(userEmail, MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW)
                .orElse(null);

        int timesAwarded = (milestone == null) ? 0 : milestone.getTimesAwarded();

        // nextRewardAt: 10, 20, 30...
        int nextRewardAt = ((qualified / 10) + 1) * 10;
        if (qualified > 0 && qualified % 10 == 0) {
            nextRewardAt = qualified + 10;
        }

        int remaining = Math.max(0, nextRewardAt - qualified);

        return new MilestoneProgressDto(
                MilestoneType.TEN_NEW_CUSTOMERS_WITH_REVIEW.name(),
                qualified,
                timesAwarded,
                nextRewardAt,
                remaining
        );
    }
}
