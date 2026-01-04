// src/main/java/com/rycus/Rycus_backend/review/ReviewService.java
package com.rycus.Rycus_backend.review;

import com.rycus.Rycus_backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Devuelve reviews de un customer (más nuevos primero)
    public List<Review> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    // Crear review (confiando en createdBy que viene del frontend)
    public Review createReview(Review review) {

        // DEBUG: para ver qué llega desde el frontend
        System.out.println("=== NEW REVIEW ===");
        System.out.println("createdBy (body): " + review.getCreatedBy());
        System.out.println("userEmail (body): " + review.getUserEmail());

        // Si createdBy viene vacío pero tenemos userEmail,
        // usamos el prefijo del email como "nombre"
        if (review.getCreatedBy() == null || review.getCreatedBy().isBlank()) {
            String email = review.getUserEmail();
            if (email != null && !email.isBlank()) {
                String beforeAt = email.split("@")[0];
                review.setCreatedBy(beforeAt);
            }
        }

        // El @PrePersist de Review se encarga de createdAt
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}
