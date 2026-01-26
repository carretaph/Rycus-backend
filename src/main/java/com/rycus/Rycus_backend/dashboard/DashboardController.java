package com.rycus.Rycus_backend.dashboard;

import com.rycus.Rycus_backend.milestone.MilestoneProgressDto;
import com.rycus.Rycus_backend.milestone.MilestoneService;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {

    private final MilestoneService milestoneService;
    private final UserRepository userRepository;

    public DashboardController(MilestoneService milestoneService, UserRepository userRepository) {
        this.milestoneService = milestoneService;
        this.userRepository = userRepository;
    }

    @GetMapping("/milestone")
    public ResponseEntity<MilestoneProgressDto> getDashboardMilestone(Authentication authentication) {

        // ✅ si no hay auth, devolver DTO seguro (antes: empty())
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.ok(MilestoneProgressDto.unauthenticated());
        }

        // En muchos setups, getName() es el "username" (a veces email, a veces otra cosa)
        String raw = authentication.getName().trim();
        if (raw.isEmpty()) {
            return ResponseEntity.ok(MilestoneProgressDto.unauthenticated());
        }

        String userEmail = raw.toLowerCase(Locale.ROOT);

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(MilestoneProgressDto.unauthenticated());
        }

        Long userId = userOpt.get().getId();

        // ✅ devolver progreso real
        try {
            MilestoneProgressDto dto =
                    milestoneService.getTenCustomerMilestoneProgress(userId, userEmail);
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok(MilestoneProgressDto.safeFallback());
        }
    }
}
