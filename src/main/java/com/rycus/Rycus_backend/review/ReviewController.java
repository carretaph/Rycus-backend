package com.rycus.Rycus_backend.review;

import com.rycus.Rycus_backend.customer.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;
    private final CustomerService customerService;

    public ReviewController(ReviewService reviewService, CustomerService customerService) {
        this.reviewService = reviewService;
        this.customerService = customerService;
    }

    // =========================================================
    // GET /customers/{customerId}/reviews?userEmail=...
    // =========================================================
    @GetMapping("/customers/{customerId}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviewsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        // Si no mandan userEmail, devolvemos todos los reviews del customer (para admin o vista general)
        List<ReviewDto> dtos = reviewService.getReviewsForCustomer(customerId, userEmail);
        return ResponseEntity.ok(dtos);
    }

    // =========================================================
    // POST /customers/{customerId}/reviews
    // Body: Review (viene con ratings/comment y puede venir userEmail)
    // =========================================================
    @PostMapping("/customers/{customerId}/reviews")
    public ResponseEntity<ReviewDto> createReviewForCustomer(
            @PathVariable Long customerId,
            @RequestBody Review reviewRequest
    ) {
        // ✅ Normaliza: si userEmail viene, úsalo como fuente
        String userEmail = (reviewRequest.getUserEmail() != null && !reviewRequest.getUserEmail().isBlank())
                ? reviewRequest.getUserEmail()
                : null;

        // fallback a createdBy
        if (userEmail == null || userEmail.isBlank()) {
            userEmail = reviewRequest.getCreatedBy();
        }

        // ✅ crea review (service normaliza createdBy y aplica anti-spam)
        ReviewDto dto = reviewService.createReview(customerId, reviewRequest, userEmail);

        // ✅ Regla Rycus: si dejó review, este customer se agrega a "My Customers"
        if (userEmail != null && !userEmail.isBlank()) {
            customerService.linkCustomerToUserById(userEmail.trim().toLowerCase(), customerId);
        }

        return ResponseEntity.ok(dto);
    }

    // =========================================================
    // DELETE /reviews/{id}
    // =========================================================
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
