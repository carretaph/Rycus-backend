package com.rycus.Rycus_backend.milestone;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/milestones")
@CrossOrigin
public class MilestoneController {

    private final MilestoneService milestoneService;
    private final UserRepository userRepository;

    public MilestoneController(
            MilestoneService milestoneService,
            UserRepository userRepository
    ) {
        this.milestoneService = milestoneService;
        this.userRepository = userRepository;
    }

    /**
     * Ejemplo:
     * GET /milestones/progress?email=alberto@gmail.com
     *
     * Respuesta:
     * {
     *   "milestoneType": "TEN_NEW_CUSTOMERS_WITH_REVIEW",
     *   "qualifiedCustomers": 3,
     *   "timesAwarded": 0,
     *   "nextRewardAt": 10,
     *   "remaining": 7
     * }
     */
    @GetMapping("/progress")
    public ResponseEntity<MilestoneProgressDto> getProgress(
            @RequestParam("email") String email
    ) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "email is required"
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        MilestoneProgressDto progress =
                milestoneService.getTenCustomerMilestoneProgress(
                        user.getId(),
                        normalizedEmail
                );

        return ResponseEntity.ok(progress);
    }
}
