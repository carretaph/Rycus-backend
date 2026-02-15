package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.dto.SafeUserDto;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<SafeUserDto> me(@RequestParam("email") String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

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

    @GetMapping("/{id}")
    public ResponseEntity<SafeUserDto> byId(@PathVariable("id") Long id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(SafeUserDto.from(user));
    }

    /**
     * =========================================================
     * GET /users/search?q=...   (viejo)
     * GET /users/search?query=... (tu frontend actual)
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
