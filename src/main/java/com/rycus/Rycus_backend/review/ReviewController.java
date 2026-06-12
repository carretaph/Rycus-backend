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

                String outcomeText = formatOutcome(dto.getOutcome());
                String reasonText = formatReasonNotSold(dto.getReasonNotSold());
                String serviceText = formatService(dto.getServiceQuoted());

                boolean isAnonymous =
                        reviewRequest.getCreatedBy() == null ||
                                reviewRequest.getCreatedBy().isBlank() ||
                                reviewRequest.getCreatedBy().equalsIgnoreCase("Anonymous reviewer");

                PostCreateRequest post = new PostCreateRequest();

                if (isAnonymous) {
                    post.setAuthorEmail("anonymous@rycus.app");
                    post.setAuthorName("Anonymous reviewer");
                } else {
                    post.setAuthorEmail(userEmail.trim().toLowerCase());
                    post.setAuthorName(reviewerName);
                }

                String text =
                        "⭐ New Customer Review\n\n" +
                                "Customer: " + customerName + "\n" +
                                "Rating: " + rating + "/5 ⭐";

                if (outcomeText != null && !outcomeText.isBlank()) {
                    text += "\nOutcome: " + outcomeText;
                }

                if (serviceText != null && !serviceText.isBlank()) {
                    text += "\nService: " + serviceText;
                }

                if (reasonText != null && !reasonText.isBlank()) {
                    text += "\nReason not sold: " + reasonText;
                }

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

    private String formatOutcome(ReviewOutcome outcome) {
        if (outcome == null) return null;

        return switch (outcome) {
            case SOLD -> "Sold";
            case NOT_SOLD -> "Not sold";
            case NO_SHOW -> "No show";
            case CANCELLED -> "Cancelled";
            case RESCHEDULED -> "Rescheduled";
            case BAD_LEAD -> "Bad lead";
            case STILL_THINKING -> "Still thinking";
        };
    }

    private String formatReasonNotSold(ReasonNotSold reason) {
        if (reason == null) return null;

        return switch (reason) {
            case PRICE -> "Price";
            case NEEDED_SPOUSE -> "Needed spouse";
            case CREDIT_ISSUE -> "Credit issue";
            case JUST_SHOPPING -> "Just shopping";
            case NOT_READY -> "Not ready";
            case WENT_WITH_ANOTHER_COMPANY -> "Went with another company";
            case OTHER -> "Other";
        };
    }

    private String formatService(String service) {
        if (service == null || service.isBlank()) return null;

        return switch (service.trim()) {
            case "AIRBNB" -> "Airbnb Hosts / Short-Term Rentals";
            case "ALARM_SYSTEMS" -> "Alarm Systems";
            case "AUTO_DETAILING" -> "Auto Detailing";
            case "AUTO_REPAIR" -> "Auto Repair Shops";
            case "BEAUTY_SALON" -> "Beauty Salons / Barbers";
            case "CAFE" -> "Cafés / Coffee Shops";
            case "CAR_DEALERSHIP" -> "Car Dealerships";
            case "CLEANING" -> "Cleaning Services";
            case "CLOTHING_STORE" -> "Clothing Stores";
            case "COMMERCIAL_SUPPLY" -> "Commercial Supply";
            case "CONVENIENCE_STORE" -> "Convenience Stores";
            case "CONSULTING" -> "Consulting";
            case "DAYCARE" -> "Daycare / Childcare";
            case "DRYWALL" -> "Drywall";
            case "ELECTRICAL" -> "Electrical";
            case "ELECTRONICS_STORE" -> "Electronics Stores";
            case "FENCING" -> "Fencing";
            case "FINANCE" -> "Accounting / Finance";
            case "FIRE_RESTORATION" -> "Fire Restoration";
            case "FITNESS" -> "Gyms / Fitness Studios";
            case "FLOORING" -> "Flooring";
            case "FOOD_TRUCK" -> "Food Trucks";
            case "FURNITURE_STORE" -> "Furniture Stores";
            case "HOME_INSPECTION" -> "Home Inspectors";
            case "HVAC" -> "HVAC";
            case "INSULATION" -> "Insulation";
            case "INSURANCE_CLAIMS" -> "Storm / Insurance Claims";
            case "IT_SERVICES" -> "IT Services";
            case "JEWELRY_STORE" -> "Jewelry Stores";
            case "LANDSCAPING" -> "Landscaping";
            case "LEGAL" -> "Legal Services";
            case "LOGISTICS" -> "Logistics / Delivery";
            case "MANUFACTURING" -> "Manufacturing";
            case "MARKETING" -> "Marketing / Advertising";
            case "MOLD_REMEDIATION" -> "Mold Remediation";
            case "NAIL_SALON" -> "Nail Salons";
            case "PAINTING" -> "Painting";
            case "PEST_CONTROL" -> "Pest Control";
            case "PET_GROOMING" -> "Pet Grooming";
            case "PET_STORE" -> "Pet Stores";
            case "PLUMBING" -> "Plumbing";
            case "POOLS_SPAS" -> "Pools & Spas";
            case "PROPERTY_MANAGEMENT" -> "Property Management";
            case "REAL_ESTATE" -> "Real Estate Agents";
            case "REMODELING" -> "Remodeling";
            case "RESTAURANT" -> "Restaurants";
            case "RETAIL_GENERAL" -> "Retail Stores (General)";
            case "ROOFING" -> "Roofing";
            case "SECURITY_SYSTEMS" -> "Security Systems";
            case "SMART_HOME_SECURITY" -> "Smart Home Security";
            case "SOLAR" -> "Solar";
            case "SUPERMARKET" -> "Supermarkets";
            case "SURVEILLANCE" -> "Surveillance / Cameras";
            case "TIRE_SHOP" -> "Tire Shops";
            case "WAREHOUSING" -> "Warehousing";
            case "WATER_RESTORATION" -> "Water Damage Restoration";
            case "WINDOWS_DOORS" -> "Windows & Doors";
            case "WHOLESALE" -> "Wholesale";
            case "OTHER" -> "Other";
            default -> service.trim();
        };
    }
}