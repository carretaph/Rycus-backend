package com.rycus.Rycus_backend.review;

import com.rycus.Rycus_backend.customer.Customer;
import com.rycus.Rycus_backend.repository.CustomerRepository;
import com.rycus.Rycus_backend.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;

    public ReviewService(ReviewRepository reviewRepository, CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
    }

    // =========================================================
    // ✅ GET reviews for a customer (optionally filtered by user)
    // Used by ReviewController.getReviewsByCustomer(...)
    // =========================================================
    public List<ReviewDto> getReviewsForCustomer(Long customerId, String userEmail) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }

        // Si mandan userEmail, filtramos por ese usuario
        if (userEmail != null && !userEmail.trim().isBlank()) {
            String email = userEmail.trim().toLowerCase();
            return reviewRepository
                    .findByCustomerIdAndCreatedByFetchCustomer(customerId, email)
                    .stream()
                    .map(ReviewDto::new)
                    .toList();
        }

        // Si no mandan userEmail, devolvemos todos los reviews del customer
        return reviewRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ReviewDto::new)
                .toList();
    }

    // =========================================================
    // ✅ CREATE review (anti-spam + createdBy normalized)
    // Used by ReviewController.createReviewForCustomer(...)
    // =========================================================
    public ReviewDto createReview(Long customerId, Review reviewRequest, String userEmail) {

        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }

        if (userEmail == null || userEmail.trim().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userEmail is required");
        }

        String emailNormalized = userEmail.trim().toLowerCase();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // Anti-spam: mismo usuario + mismo customer + 5 segundos
        Review last = reviewRepository
                .findTopByCustomer_IdAndCreatedByIgnoreCaseOrderByCreatedAtDesc(customerId, emailNormalized)
                .orElse(null);

        if (last != null && last.getCreatedAt() != null) {
            OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(5);
            if (last.getCreatedAt().isAfter(cutoff)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Duplicate submission detected. Please wait a few seconds and try again."
                );
            }
        }

        Review r = new Review();
        r.setCustomer(customer);
        r.setCreatedBy(emailNormalized);

        r.setRatingOverall(reviewRequest.getRatingOverall());
        r.setRatingPayment(reviewRequest.getRatingPayment());
        r.setRatingBehavior(reviewRequest.getRatingBehavior());
        r.setRatingCommunication(reviewRequest.getRatingCommunication());
        r.setComment(reviewRequest.getComment());

        // createdAt lo setea @PrePersist si viene null (UTC)
        Review saved = reviewRepository.save(r);

        return new ReviewDto(saved);
    }

    // =========================================================
    // ✅ DELETE review by id
    // Used by ReviewController.deleteReview(...)
    // =========================================================
    public void deleteReview(Long reviewId) {
        if (reviewId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reviewId is required");
        }

        if (!reviewRepository.existsById(reviewId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }

        reviewRepository.deleteById(reviewId);
    }
}
