package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.customer.Customer;
import com.rycus.Rycus_backend.repository.ReviewRepository;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.review.Review;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ fallback seguro (por si el encoder inyectado no reconoce el hash)
    private final BCryptPasswordEncoder bcryptFallback = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository,
                       ReviewRepository reviewRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================
    // ✅ GET /users/by-email?email=...
    // =========================================
    @Transactional(readOnly = true)
    public UserMiniDto getUserMiniByEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(emailNormalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String fullName = (user.getFullName() != null && !user.getFullName().trim().isEmpty())
                ? user.getFullName().trim()
                : user.getEmail();

        return new UserMiniDto(
                user.getEmail(),
                fullName,
                user.getAvatarUrl()
        );
    }

    // =========================================
    // REGISTRO
    // =========================================
    @Transactional
    public User registerUser(String fullName, String email, String rawPassword, String ref) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(emailNormalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        if (rawPassword.trim().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        User user = new User();
        user.setFullName(fullName != null ? fullName.trim() : null);
        user.setEmail(emailNormalized);

        // ✅ BCrypt
        user.setPassword(passwordEncoder.encode(rawPassword));

        if (user.getRole() == null) {
            user.setRole("USER");
        }

        // trial 30 días
        Instant now = Instant.now();
        Instant trialEnd = now.plus(30, ChronoUnit.DAYS);

        user.setPlanType(PlanType.FREE_TRIAL);
        user.setTrialEndsAt(trialEnd);
        user.setSubscriptionEndsAt(trialEnd);
        user.setFreeMonthsBalance(0);

        // referral code
        user.setReferralCode(generateUniqueReferralCode());

        // referredBy (si llega ref)
        String refNormalized = safeTrim(ref);
        if (refNormalized != null) {
            Optional<User> referrerOpt = userRepository.findByReferralCodeIgnoreCase(refNormalized);
            if (referrerOpt.isPresent()) {
                User referrer = referrerOpt.get();
                if (referrer.getEmail() != null
                        && !referrer.getEmail().equalsIgnoreCase(emailNormalized)) {
                    user.setReferredByEmail(referrer.getEmail());
                }
            }
        }

        return userRepository.save(user);
    }

    // =========================================
    // LOGIN (robusto)
    // =========================================
    @Transactional(readOnly = true)
    public User login(String email, String rawPassword) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(emailNormalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String stored = user.getPassword();
        if (stored == null || stored.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // 🔎 LOGS TEMPORALES (para ver qué pasa de verdad)
        System.out.println("🔐 LOGIN attempt email=" + emailNormalized);
        System.out.println("🔐 stored hash startsWith=$2? " + stored.startsWith("$2"));

        boolean ok = false;

        // 1) intento con el encoder inyectado
        try {
            ok = passwordEncoder.matches(rawPassword, stored);
            System.out.println("🔐 matches() with injected PasswordEncoder => " + ok);
        } catch (Exception ex) {
            System.out.println("🔐 injected PasswordEncoder threw: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }

        // 2) fallback BCrypt directo (por si el encoder es delegating o distinto)
        if (!ok) {
            try {
                boolean ok2 = bcryptFallback.matches(rawPassword, stored);
                System.out.println("🔐 matches() with BCrypt fallback => " + ok2);
                ok = ok2;
            } catch (Exception ex) {
                System.out.println("🔐 BCrypt fallback threw: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
        }

        // 3) compat legacy: si alguna vez guardaste plain-text (solo por transición)
        if (!ok && stored.equals(rawPassword)) {
            System.out.println("⚠️ legacy plain-text password match (temporary)");
            ok = true;
        }

        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return user;
    }

    // =========================================
    // Subscription status
    // =========================================
    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(String email) {
        String e = safeTrim(email);
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        User user = userRepository.findByEmailIgnoreCase(e.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean active = isActive(user);

        return new SubscriptionStatusResponse(
                active,
                user.getPlanType(),
                user.getTrialEndsAt(),
                user.getSubscriptionEndsAt(),
                user.getFreeMonthsBalance(),
                user.getReferralCode()
        );
    }

    private boolean isActive(User user) {
        if (user == null) return false;
        if (user.getPlanType() == PlanType.FREE_LIFETIME) return true;
        Instant end = user.getSubscriptionEndsAt();
        if (end == null) return false;
        return end.isAfter(Instant.now());
    }

    // =========================================
    // CHANGE EMAIL
    // =========================================
    @Transactional
    public void changeEmail(String currentEmail, String newEmail, String password) {

        if (currentEmail == null || currentEmail.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentEmail is required");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newEmail is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
        }

        String oldE = currentEmail.trim().toLowerCase(Locale.ROOT);
        String newE = newEmail.trim().toLowerCase(Locale.ROOT);

        if (oldE.equalsIgnoreCase(newE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New email must be different");
        }

        if (!newE.contains("@") || !newE.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        User user = userRepository.findByEmailIgnoreCase(oldE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean ok = false;
        try {
            ok = passwordEncoder.matches(password, user.getPassword());
        } catch (Exception ignored) {}
        if (!ok) ok = bcryptFallback.matches(password, user.getPassword());

        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        if (userRepository.existsByEmailIgnoreCase(newE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        user.setEmail(newE);
        userRepository.save(user);
    }

    // =========================================
    // SEARCH
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
    // PROFILE
    // =========================================
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long id) {

        User user = userRepository.findById(id)
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
    // UPDATE MY PROFILE
    // =========================================
    @Transactional
    public UserProfileDto updateMyProfile(String email, UpdateMyProfileRequest body) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        String emailNormalized = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(emailNormalized)
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

    // =========================================
    // Helpers
    // =========================================
    private String safeTrim(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private String generateUniqueReferralCode() {
        for (int i = 0; i < 10; i++) {
            String code = "RYCUS-" + randomAlphaNum(6);
            boolean exists = userRepository.existsByReferralCodeIgnoreCase(code);
            if (!exists) return code;
        }
        return "RYCUS-" + randomAlphaNum(10);
    }

    private String randomAlphaNum(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int idx = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
