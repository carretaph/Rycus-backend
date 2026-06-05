package com.rycus.Rycus_backend.review;

import com.rycus.Rycus_backend.customer.CustomerService;
import com.rycus.Rycus_backend.post.PostCreateRequest;
import com.rycus.Rycus_backend.post.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;
    private final CustomerService customerService;
    private final PostService postService;

    public ReviewController(
            ReviewService reviewService,
            CustomerService customerService,
            PostService postService
    ) {
        this.reviewService = reviewService;
        this.customerService = customerService;
        this.postService = postService;
    }

    @GetMapping("/customers/{customerId}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviewsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        List<ReviewDto> dtos = reviewService.getReviewsForCustomer(customerId, userEmail);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/customers/{customerId}/reviews")
    public ResponseEntity<ReviewDto> createReviewForCustomer(
            @PathVariable Long customerId,
            @RequestBody Review reviewRequest
    ) {
        String userEmail = (reviewRequest.getUserEmail() != null && !reviewRequest.getUserEmail().isBlank())
                ? reviewRequest.getUserEmail()
                : null;

        if (userEmail == null || userEmail.isBlank()) {
            userEmail = reviewRequest.getCreatedBy();
        }

        ReviewDto dto = reviewService.createReview(customerId, reviewRequest, userEmail);

        // Create automatic Wall activity post
        if (userEmail != null && !userEmail.isBlank()) {
            try {
                String reviewerName =
                        (reviewRequest.getCreatedBy() != null && !reviewRequest.getCreatedBy().isBlank())
                                ? reviewRequest.getCreatedBy()
                                : userEmail;

                String customerName =
                        dto.getCustomerName() != null && !dto.getCustomerName().isBlank()
                                ? dto.getCustomerName()
                                : "Customer";

                String rating =
                        dto.getRatingOverall() != null
                                ? dto.getRatingOverall().toString()
                                : "?";

                String comment =
                        dto.getComment() != null
                                ? dto.getComment().trim()
                                : "";

                PostCreateRequest post = new PostCreateRequest();
                post.setAuthorEmail(userEmail.trim().toLowerCase());
                post.setAuthorName(reviewerName);

                String text =
                        "⭐ New Customer Review\n\n" +
                                "Customer: " + customerName + "\n" +
                                "Rating: " + rating + "/5 ⭐";

                if (!comment.isBlank()) {
                    text += "\n\n\"" + comment + "\"";
                }

                post.setText(text);
                postService.create(post);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            customerService.linkCustomerToUserById(userEmail.trim().toLowerCase(), customerId);
        }

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }
}