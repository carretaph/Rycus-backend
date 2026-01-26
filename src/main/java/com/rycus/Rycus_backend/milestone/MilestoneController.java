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

    public MilestoneController(MilestoneService milestoneService, UserRepository userRepository) {
        this.milestoneService = milestoneService;
        this.userRepository = userRepository;
    }

    /**
     * GET /milestones/progress?email=...
     *
     * Siempre intenta devolver progreso.
     * Si algo falla internamente, devuelve un progreso "seguro"
     * para que el frontend nunca muestre 500.
     */
    @GetMapping("/progress")
    public ResponseEntity<MilestoneProgressDto> progress(@RequestParam("email") String email) {

        if (email == null || email.trim().isEmpty()) {
            // Para el dashboard, esto es "no autenticado / sin email"
            return ResponseEntity.ok(MilestoneProgressDto.unauthenticated());
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            MilestoneProgressDto dto =
                    milestoneService.getTenCustomerMilestoneProgress(user.getId(), normalized);
            return ResponseEntity.ok(dto);

        } catch (Exception ex) {
            // ✅ Nunca romper el endpoint por un error en query/milestone
            System.out.println("❌ Milestone progress failed for email=" + normalized + " userId=" + user.getId());
            ex.printStackTrace();

            return ResponseEntity.ok(MilestoneProgressDto.safeFallback());
        }
    }
}
