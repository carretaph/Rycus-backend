package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.customer.Customer;
import com.rycus.Rycus_backend.repository.ReviewRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.review.Review;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository,
                       ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    // =========================================
    // REGISTRO (AuthController.register)
    // =========================================
    @Transactional
    public User registerUser(String fullName, String email, String rawPassword) {

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(emailNormalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        User user = new User();
        user.setFullName(fullName != null ? fullName.trim() : null);
        user.setEmail(emailNormalized);
        user.setPassword(rawPassword);

        if (user.getRole() == null) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    // =========================================
    // LOGIN (AuthController.login)
    // =========================================
    @Transactional(readOnly = true)
    public User login(String email, String rawPassword) {

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(emailNormalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!rawPassword.equals(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return user;
    }

    // =========================================
    // 🔍 GET /users/search?q=...
    // =========================================
    @Transactional(readOnly = true)
    public List<UserSummaryDto> searchUsers(String query) {

        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) return List.of();

        List<User> users = userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q);

        return users.stream()
                .map(u -> {
                    UserSummaryDto dto = new UserSummaryDto();
                    dto.setId(u.getId());
                    dto.setFullName(u.getFullName());
                    dto.setEmail(u.getEmail());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================
    // 👤 GET /users/{id}
    // Perfil público + reviews
    // =========================================
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long id) {

        User user = userRepository.findById(id)
                // ✅ CAMBIO CLAVE: 404 en vez de 500
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String emailLower = (user.getEmail() == null ? "" : user.getEmail().toLowerCase(Locale.ROOT));
        List<Review> reviews = emailLower.isBlank()
                ? List.of()
                : reviewRepository.findByCreatedByIgnoreCase(emailLower);

        int totalReviews = reviews.size();
        double averageRating = 0.0;

        if (!reviews.isEmpty()) {
            double sum = 0.0;
            int count = 0;

            for (Review r : reviews) {
                Integer val = r.getRatingOverall();
                if (val == null) val = r.getRatingPayment();
                if (val == null) val = 0;
                sum += val;
                count++;
            }

            if (count > 0) averageRating = sum / count;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<UserReviewDto> reviewDtos = reviews.stream()
                .map(r -> {
                    Customer c = r.getCustomer();

                    UserReviewDto dto = new UserReviewDto();
                    dto.setId(r.getId());
                    dto.setCustomerId(c != null ? c.getId() : null);
                    dto.setCustomerName(c != null ? c.getFullName() : "Unknown");
                    dto.setRatingOverall(r.getRatingOverall());
                    dto.setRatingPayment(r.getRatingPayment());
                    dto.setRatingBehavior(r.getRatingBehavior());
                    dto.setRatingCommunication(r.getRatingCommunication());
                    dto.setComment(r.getComment());
                    if (r.getCreatedAt() != null) {
                        dto.setCreatedAt(r.getCreatedAt().format(fmt));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        // ✅ public fields
        dto.setPhone(user.getPhone());
        dto.setBusinessName(user.getBusinessName());
        dto.setIndustry(user.getIndustry());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setAvatarUrl(user.getAvatarUrl());

        dto.setTotalReviews(totalReviews);
        dto.setAverageRating(averageRating);
        dto.setReviews(reviewDtos);

        return dto;
    }

    // =========================================
    // ✅ PUT /users/me?email=...
    // Actualizar MI perfil persistente en DB
    // =========================================
    @Transactional
    public UserProfileDto updateMyProfile(String email, UpdateMyProfileRequest body) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(emailNormalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (body != null) {
            if (body.getFullName() != null) user.setFullName(body.getFullName().trim());
            if (body.getPhone() != null) user.setPhone(body.getPhone().trim());
            if (body.getBusinessName() != null) user.setBusinessName(body.getBusinessName().trim());
            if (body.getIndustry() != null) user.setIndustry(body.getIndustry().trim());
            if (body.getCity() != null) user.setCity(body.getCity().trim());
            if (body.getState() != null) user.setState(body.getState().trim());
            if (body.getAvatarUrl() != null) user.setAvatarUrl(body.getAvatarUrl());
        }

        userRepository.save(user);

        return getUserProfile(user.getId());
    }
}
