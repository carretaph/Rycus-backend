package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.dto.SafeUserDto;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // DTO para update /users/me
    // =========================
    public static class UpdateMeRequest {
        public String fullName;
        public String phone;
        public String avatarUrl;
        public String businessName;
        public String industry;
        public String city;
        public String state;

        // ✅ Referral Fee
        public Boolean offersReferralFee;
        public String referralFeeType;      // "FLAT" o "PERCENT"
        public BigDecimal referralFeeValue; // 25.00, etc
        public String referralFeeNotes;
    }

    /**
     * =========================================================
     * GET /users/me   (USA EL JWT)
     * =========================================================
     */
    @GetMapping("/me")
    public ResponseEntity<SafeUserDto> me(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SafeUserDto dto = SafeUserDto.from(user);

        if (dto != null) {
            if (dto.getPlanType() == null && user.getPlanType() != null) {
                dto.setPlanType(user.getPlanType().name());
            }
            if (dto.getSubscriptionStatus() == null && user.getSubscriptionStatus() != null) {
                dto.setSubscriptionStatus(user.getSubscriptionStatus());
            }
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * =========================================================
     * PUT /users/me   (USA EL JWT) ✅ ACTUALIZA PERFIL + REFERRAL
     * =========================================================
     */
    @PutMapping("/me")
    public ResponseEntity<SafeUserDto> updateMe(Authentication authentication,
                                                @RequestBody UpdateMeRequest body) {

        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // --- campos normales (solo si vienen en body) ---
        if (body != null) {
            if (body.fullName != null) user.setFullName(body.fullName.trim());
            if (body.phone != null) user.setPhone(body.phone.trim());
            if (body.avatarUrl != null) user.setAvatarUrl(body.avatarUrl.trim());
            if (body.businessName != null) user.setBusinessName(body.businessName.trim());
            if (body.industry != null) user.setIndustry(body.industry.trim());
            if (body.city != null) user.setCity(body.city.trim());
            if (body.state != null) user.setState(body.state.trim());

            // --- ✅ referral fee ---
            if (body.offersReferralFee != null) {
                user.setOffersReferralFee(body.offersReferralFee);
                if (!body.offersReferralFee) {
                    // si apaga, limpia todo
                    user.setReferralFeeType(null);
                    user.setReferralFeeValue(null);
                    user.setReferralFeeNotes(null);
                }
            }
            if (body.referralFeeType != null) user.setReferralFeeType(body.referralFeeType.trim());
            if (body.referralFeeValue != null) user.setReferralFeeValue(body.referralFeeValue);
            if (body.referralFeeNotes != null) user.setReferralFeeNotes(body.referralFeeNotes.trim());
        }

        userRepository.save(user);

        return ResponseEntity.ok(SafeUserDto.from(user));
    }

    /**
     * =========================================================
     * GET /users/by-email?email=...
     * =========================================================
     */
    @GetMapping("/by-email")
    public ResponseEntity<SafeUserDto> byEmail(@RequestParam("email") String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(SafeUserDto.from(user));
    }

    /**
     * =========================================================
     * GET /users/{id}
     * (Regex para que NO choque con /me)
     * =========================================================
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<SafeUserDto> byId(@PathVariable("id") Long id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(SafeUserDto.from(user));
    }

    /**
     * =========================================================
     * GET /users/search?q=...
     * GET /users/search?query=...
     * =========================================================
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserMiniDto>> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "query", required = false) String query
    ) {
        String raw = (q != null && !q.isBlank()) ? q : query;
        String term = (raw == null) ? "" : raw.trim();

        if (term.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<UserMiniDto> results = userRepository.searchMini(term);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/me-test")
    public ResponseEntity<String> testDeploy() {
        return ResponseEntity.ok("NEW VERSION DEPLOYED ✅");
    }
}