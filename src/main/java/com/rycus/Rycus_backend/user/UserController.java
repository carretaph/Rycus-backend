// src/main/java/com/rycus/Rycus_backend/user/UserController.java
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
     * GET /users/me?email=...
     * Devuelve SIEMPRE SafeUserDto (incluye planType y subscriptionStatus)
     */
    @GetMapping("/me")
    public ResponseEntity<SafeUserDto> me(@RequestParam("email") String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SafeUserDto dto = SafeUserDto.from(user);

        // Safety: si por alguna razón planType viniera null, no dejes que el frontend se rompa
        // (no debería pasar, pero evita que te mande a Unlock por un null raro)
        if (dto != null && dto.getPlanType() == null && user.getPlanType() != null) {
            dto.setPlanType(user.getPlanType().name());
        }

        return ResponseEntity.ok(dto);
    }
}
