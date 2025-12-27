package com.rycus.Rycus_backend.user;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Spring inyecta UserService aquí
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ================================
    // 🔍 GET /users/search?q=texto
    // ================================
    @GetMapping("/search")
    public List<UserSummaryDto> searchUsers(@RequestParam("q") String q) {
        return userService.searchUsers(q);
    }

    // ================================
    // 👤 GET /users/{id}
    // Perfil + reviews del usuario
    // ================================
    @GetMapping("/{id}")
    public UserProfileDto getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }
}
