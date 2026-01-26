// src/main/java/com/rycus/Rycus_backend/review/ReviewService.java
package com.rycus.Rycus_backend.review;

import com.rycus.Rycus_backend.customer.CustomerService;
import com.rycus.Rycus_backend.milestone.MilestoneService;
import com.rycus.Rycus_backend.repository.ReviewRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerService customerService;
    private final UserRepository userRepository;
    private final MilestoneService milestoneService;

    public ReviewService(
            ReviewRepository reviewRepository,
            CustomerService customerService,
            UserRepository userRepository,
            MilestoneService milestoneService
    ) {
        this.reviewRepository = reviewRepository;
        this.customerService = customerService;
        this.userRepository = userRepository;
        this.milestoneService = milestoneService;
    }

    // =========================================================
    // Obtener reviews de un customer (más nuevos primero)
    // (GLOBAL) - mantiene compatibilidad si lo usas en otro lado
    // =========================================================
    public List<Review> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    // =========================================================
    // ✅ NUEVO: Obtener reviews de un customer para un usuario
    // Devuelve DTOs para evitar LazyInitialization / ciclos JSON
    // Usa JOIN FETCH customer en el repository
    // =========================================================
    public List<ReviewDto> getReviewsForCustomer(Long customerId, String userEmail) {
        if (customerId == null) return List.of();

        String email = safeTrim(userEmail);
        if (email == null) return List.of();

        String normalized = email.toLowerCase(Locale.ROOT);

        return reviewRepository
                .findByCustomerIdAndCreatedByFetchCustomer(customerId, normalized)
                .stream()
                .map(ReviewDto::new)
                .toList();
    }

    // =========================================================
    // Crear review
    // ✅ Permite múltiples reviews por el mismo customer
    // ❌ NO bloquea historial
    // ✅ Solo evita doble-submit inmediato (anti double-click)
    // =========================================================
    public Review createReview(Review review) {

        // ============================
        // 0) Validaciones mínimas
        // ============================
        if (review == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review body is required"
            );
        }

        if (review.getCustomer() == null || review.getCustomer().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "customer.id is required"
            );
        }

        String userEmail = safeTrim(review.getUserEmail());
        if (userEmail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userEmail is required"
            );
        }

        String emailNormalized = userEmail.toLowerCase(Locale.ROOT);
        Long customerId = review.getCustomer().getId();

        // ============================
        // 1) createdBy consistente (EMAIL)
        // ============================
        review.setCreatedBy(emailNormalized);

        // ============================
        // 2) Anti-spam (NO anti-historial)
        //    Bloquea solo si el MISMO usuario
        //    envía otro review al MISMO customer
        //    dentro de 5 segundos
        // ============================
        try {
            Review last = reviewRepository
                    .findTopByCustomer_IdAndCreatedByIgnoreCaseOrderByCreatedAtDesc(
                            customerId,
                            emailNormalized
                    )
                    .orElse(null);

            if (last != null && last.getCreatedAt() != null) {
                LocalDateTime cutoff = LocalDateTime.now().minusSeconds(5);
                if (last.getCreatedAt().isAfter(cutoff)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Duplicate submission detected. Please wait a few seconds and try again."
                    );
                }
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ignored) {
            // Si este check falla por cualquier razón,
            // NO bloqueamos la creación del review
        }

        // ============================
        // 3) Guardar review
        // ============================
        Review saved = reviewRepository.save(review);

        // ============================
        // 4) Link automático a "My Customers"
        // ============================
        customerService.linkCustomerToUserById(
                emailNormalized,
                customerId
        );

        // ============================
        // 5) Milestone (NO debe romper el flujo)
        //    Cuenta CUSTOMERS únicos, no reviews
        // ============================
        Long userId = userRepository
                .findByEmailIgnoreCase(emailNormalized)
                .map(User::getId)
                .orElse(null);

        try {
            if (userId != null) {
                milestoneService.evaluateTenCustomerMilestone(
                        userId,
                        emailNormalized
                );
            }
        } catch (Exception ex) {
            // Nunca romper creación de review por milestone
            ex.printStackTrace();
        }

        return saved;
    }

    // =========================================================
    // Eliminar review
    // =========================================================
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    // =========================================================
    // Utils
    // =========================================================
    private String safeTrim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
