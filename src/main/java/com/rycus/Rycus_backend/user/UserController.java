package com.rycus.Rycus_backend.user;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public List<UserSummaryDto> searchUsers(@RequestParam("q") String q) {
        return userService.searchUsers(q);
    }

    @GetMapping("/{id}")
    public UserProfileDto getUserProfile(@PathVariable Long id) {
        // Si el user no existe, el service debe tirar 404 (ResponseStatusException)
        // y Spring lo devuelve correcto (no 500).
        try {
            return userService.getUserProfile(id);
        } catch (ResponseStatusException e) {
            throw e;
        }
    }

    @PutMapping("/me")
    public UserProfileDto updateMyProfile(
            @RequestParam("email") String email,
            @RequestBody UpdateMyProfileRequest request
    ) {
        return userService.updateMyProfile(email, request);
    }
}
