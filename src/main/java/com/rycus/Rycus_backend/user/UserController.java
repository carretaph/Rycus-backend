package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.dto.SafeUserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * =========================================================
     * GET /users/me?email=...
     * Devuelve SIEMPRE SafeUserDto
     * Incluye:
     *  - planType
     *  - subscriptionStatus
     *  - profile data
     * =========================================================
     */
    @GetMapping("/me")
    public ResponseEntity<SafeUserDto> me(@RequestParam("email") String email) {

        // 1️⃣ Validación básica
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "email is required"
            );
        }

        // 2️⃣ Buscar usuario ignorando mayúsculas/minúsculas
        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // 3️⃣ Mapear a Safe DTO
        SafeUserDto dto = SafeUserDto.from(user);

        // 4️⃣ SAFETY NET
        // Evita que frontend mande a Unlock si algo raro viene null
        if (dto != null) {

            if (dto.getPlanType() == null && user.getPlanType() != null) {
                dto.setPlanType(user.getPlanType().name());
            }

            if (dto.getSubscriptionStatus() == null
                    && user.getSubscriptionStatus() != null) {
                dto.setSubscriptionStatus(user.getSubscriptionStatus());
            }
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * =========================================================
     * TEST ENDPOINT
     * Solo para verificar que Render deployó esta versión
     * Bórralo después si quieres
     * =========================================================
     */
    @GetMapping("/me-test")
    public ResponseEntity<String> testDeploy() {
        return ResponseEntity.ok("NEW VERSION DEPLOYED ✅");
    }
}
