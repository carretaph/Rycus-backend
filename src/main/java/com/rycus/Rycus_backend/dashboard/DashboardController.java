package com.rycus.Rycus_backend.dashboard;

import com.rycus.Rycus_backend.milestone.MilestoneProgressDto;
import com.rycus.Rycus_backend.milestone.MilestoneService;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final MilestoneService milestoneService;
    private final UserRepository userRepository;

    public DashboardController(MilestoneService milestoneService, UserRepository userRepository) {
        this.milestoneService = milestoneService;
        this.userRepository = userRepository;
    }

    @GetMapping("/milestone")
    public ResponseEntity<MilestoneProgressDto> getDashboardMilestone(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.ok(MilestoneProgressDto.empty());
        }

        // 🔐 En muchos setups, getName() es el "username" (a veces email, a veces otra cosa)
        String raw = authentication.getName().trim();
        if (raw.isEmpty()) {
            return ResponseEntity.ok(MilestoneProgressDto.empty());
        }

        String userEmail = raw.toLowerCase(Locale.ROOT);

        // Si no existe usuario por ese value, devolvemos empty (y lo vemos en whoami)
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(userEmail);
        Long userId = userOpt.map(User::getId).orElse(null);

        MilestoneProgressDto dto = milestoneService.getTenCustomerMilestoneProgress(userId, userEmail);
        return ResponseEntity.ok(dto);
    }

    // ✅ DEBUG: para ver exactamente qué trae Spring en authentication.getName()
    @GetMapping("/whoami")
    public ResponseEntity<String> whoami(Authentication authentication) {
        if (authentication == null) return ResponseEntity.ok("auth=null");
        return ResponseEntity.ok("authName=" + authentication.getName());
    }
}
