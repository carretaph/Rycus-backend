package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.customer.Customer;
import com.rycus.Rycus_backend.repository.ReviewRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.review.Review;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository,
                       ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    // ================================
    // REGISTRAR NUEVO USUARIO
    // ================================
    public User registerUser(String fullName, String email, String password, String phone) {

        String cleanEmail = email == null ? null : email.trim().toLowerCase();
        String cleanName = fullName == null ? null : fullName.trim();
        String cleanPhone = phone == null ? null : phone.trim();

        if (cleanEmail == null || cleanEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (cleanName == null || cleanName.isBlank()) {
            throw new RuntimeException("Full name is required");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        // Si el email ya existe, lanzamos error
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new RuntimeException("El email ya está registrado: " + cleanEmail);
        }

        User user = new User();
        user.setFullName(cleanName);
        user.setEmail(cleanEmail);
        user.setPassword(password); // luego encriptamos
        user.setPhone(cleanPhone);
        user.setRole("USER");

        return userRepository.save(user);
    }

    // ================================
    // LOGIN
    // ================================
    public User login(String email, String password) {

        String cleanEmail = email == null ? null : email.trim().toLowerCase();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return user;
    }

    // ================================
    // 🔍 BUSCAR USUARIOS POR NOMBRE / EMAIL
    // ================================
    public List<UserSummaryDto> searchUsers(String query) {
        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) {
            return List.of();
        }

        List<User> users = userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q);

        List<UserSummaryDto> result = new ArrayList<>();

        for (User user : users) {
            List<Review> reviews =
                    reviewRepository.findByCreatedByIgnoreCase(user.getEmail());

            long totalReviews = reviews.size();

            double averageRating = 0.0;
            if (totalReviews > 0) {
                double sum = 0.0;
                for (Review r : reviews) {
                    Integer overall = r.getRatingOverall();
                    Integer payment = r.getRatingPayment();
                    int value = (overall != null) ? overall
                            : (payment != null ? payment : 0);
                    sum += value;
                }
                averageRating = sum / totalReviews;
            }

            result.add(new UserSummaryDto(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    totalReviews,
                    averageRating
            ));
        }

        return result;
    }

    // ================================
    // 👤 PERFIL DE USUARIO + REVIEWS
    // ================================
    public UserProfileDto getUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Review> reviews =
                reviewRepository.findByCreatedByIgnoreCase(user.getEmail());

        long totalReviews = reviews.size();

        double averageRating = 0.0;
        if (totalReviews > 0) {
            double sum = 0.0;
            for (Review r : reviews) {
                Integer overall = r.getRatingOverall();
                Integer payment = r.getRatingPayment();
                int value = (overall != null) ? overall
                        : (payment != null ? payment : 0);
                sum += value;
            }
            averageRating = sum / totalReviews;
        }

        List<UserReviewDto> reviewDtos = new ArrayList<>();

        for (Review r : reviews) {
            Customer c = r.getCustomer();

            Long customerId = (c != null) ? c.getId() : null;
            String customerName =
                    (c != null && c.getFullName() != null)
                            ? c.getFullName()
                            : "Unknown customer";

            String createdAt =
                    (r.getCreatedAt() != null)
                            ? r.getCreatedAt().toString()
                            : null;

            reviewDtos.add(new UserReviewDto(
                    r.getId(),
                    customerId,
                    customerName,
                    r.getRatingOverall(),
                    r.getRatingPayment(),
                    r.getRatingBehavior(),
                    r.getRatingCommunication(),
                    r.getComment(),
                    createdAt
            ));
        }

        return new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                totalReviews,
                averageRating,
                reviewDtos
        );
    }
}
