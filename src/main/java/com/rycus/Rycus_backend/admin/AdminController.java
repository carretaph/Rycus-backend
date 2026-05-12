package com.rycus.Rycus_backend.admin;

import com.rycus.Rycus_backend.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public AdminStatsDto stats() {

        long totalUsers = userRepository.count();

        long adminUsers = userRepository.findAll()
                .stream()
                .filter(u ->
                        u.getRole() != null
                                && u.getRole().equalsIgnoreCase("ADMIN")
                )
                .count();

        long usersWithReferralFee = userRepository.findAll()
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getOffersReferralFee()))
                .count();

        return new AdminStatsDto(
                totalUsers,
                adminUsers,
                usersWithReferralFee
        );
    }

    @GetMapping("/users")
    public List<AdminUserDto> users() {

        return userRepository.findAll()
                .stream()
                .map(AdminUserDto::from)
                .toList();
    }
}