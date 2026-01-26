package com.rycus.Rycus_backend.review;

import com.rycus.Rycus_backend.customer.Customer;
import com.rycus.Rycus_backend.customer.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;
    private final CustomerService customerService;

    public ReviewController(ReviewService reviewService, CustomerService customerService) {
        this.reviewService = reviewService;
        this.customerService = customerService;
    }

    // =========================================================
    // ✅ GET /customers/{customerId}/reviews?userEmail=...
    // Devuelve DTOs para evitar LazyInitialization / ciclos JSON
    // =========================================================
    @GetMapping("/customers/{customerId}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviewsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        // Si no mandan userEmail, devolvemos vacío (dashboard siempre lo manda)
        List<ReviewDto> dtos = reviewService.getReviewsForCustomer(customerId, userEmail);
        return ResponseEntity.ok(dtos);
    }

    // =========================================================
    // POST /customers/{customerId}/reviews
    // Crea review para un customer
    // (mantengo tu lógica, pero devuelvo DTO para consistencia)
    // =========================================================
    @PostMapping("/customers/{customerId}/reviews")
    public ResponseEntity<ReviewDto> createReviewForCustomer(
            @PathVariable Long customerId,
            @RequestBody Review reviewRequest
    ) {
        // 1) Buscar el customer global
        Customer customer = customerService.getCustomerById(customerId);
        reviewRequest.setCustomer(customer);

        // 2) Si no viene createdBy pero sí userEmail, usamos userEmail como createdBy
        if ((reviewRequest.getCreatedBy() == null || reviewRequest.getCreatedBy().isBlank())
                && reviewRequest.getUserEmail() != null
                && !reviewRequest.getUserEmail().isBlank()) {
            reviewRequest.setCreatedBy(reviewRequest.getUserEmail());
        }

        // 3) Guardar el review (createReview normaliza createdBy a email)
        Review createdReview = reviewService.createReview(reviewRequest);

        // 4) ✅ Regla Rycus: si dejó review, este customer se agrega a "My Customers"
        if (reviewRequest.getUserEmail() != null && !reviewRequest.getUserEmail().isBlank()) {
            customerService.linkCustomerToUserById(reviewRequest.getUserEmail(), customerId);
        }

        // ✅ devolver DTO (evita lazy y mantiene response estable)
        return ResponseEntity.ok(new ReviewDto(createdReview));
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
