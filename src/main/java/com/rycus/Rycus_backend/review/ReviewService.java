// src/main/java/com/rycus/Rycus_backend/review/ReviewService.java
package com.rycus.Rycus_backend.review;

import com.rycus.Rycus_backend.customer.CustomerService;
import com.rycus.Rycus_backend.milestone.MilestoneService;
import com.rycus.Rycus_backend.repository.ReviewRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerService customerService;
    private final UserRepository userRepository;
    private final MilestoneService milestoneService;

    public ReviewService(ReviewRepository reviewRepository,
                         CustomerService customerService,
                         UserRepository userRepository,
                         MilestoneService milestoneService) {
        this.reviewRepository = reviewRepository;
        this.customerService = customerService;
        this.userRepository = userRepository;
        this.milestoneService = milestoneService;
    }

    // Devuelve reviews de un customer (más nuevos primero)
    public List<Review> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    // Crear review
    public Review createReview(Review review) {

        // ============================
        // 0) Validaciones mínimas
        // ============================
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review body is required");
        }
        if (review.getCustomer() == null || review.getCustomer().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customer.id is required");
        }

        String userEmail = safeTrim(review.getUserEmail());
        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail is required");
        }

        String emailNormalized = userEmail.toLowerCase(Locale.ROOT);
        Long customerId = review.getCustomer().getId();

        // ============================
        // 1) Guardar createdBy CONSISTENTE
        //    ✅ Guardamos el EMAIL
        // ============================
        review.setCreatedBy(emailNormalized);

        // ============================
        // 2) Anti-trampa (doble protección)
        //    A) check antes de guardar
        //    B) constraint UNIQUE en DB (recomendado)
        // ============================
        boolean alreadyExists = reviewRepository
                .existsByCreatedByIgnoreCaseAndCustomer_Id(emailNormalized, customerId);

        if (alreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already reviewed this customer"
            );
        }

        // ============================
        // 3) Guardar review
        // ============================
        Review saved;
        try {
            saved = reviewRepository.save(review);
        } catch (DataIntegrityViolationException ex) {
            // Si el UNIQUE constraint dispara, devolvemos 409 igual
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already reviewed this customer"
            );
        }

        // ============================
        // 4) Link automático a My Customers
        // ============================
        customerService.linkCustomerToUserById(emailNormalized, customerId);

        // ============================
        // 5) Milestone (NO debe romper creación de review)
        // ============================
        Long userId = userRepository.findByEmailIgnoreCase(emailNormalized)
                .map(User::getId)
                .orElse(null);

        try {
            if (userId != null) {
                milestoneService.evaluateTenCustomerMilestone(userId, emailNormalized);
            }
        } catch (Exception ex) {
            // ✅ Nunca romper el flow por milestone
            ex.printStackTrace();
        }

        return saved;
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    private String safeTrim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
