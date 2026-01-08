package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.user.dto.UserMiniDto;
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

    // ✅ Para MessagesPage / Inbox: /users/by-email?email=...
    @GetMapping("/by-email")
    public UserMiniDto getByEmail(@RequestParam("email") String email) {
        return userService.getUserMiniByEmail(email);
    }

    @GetMapping("/search")
    public List<UserSummaryDto> searchUsers(@RequestParam("q") String q) {
        return userService.searchUsers(q);
    }

    @GetMapping("/{id}")
    public UserProfileDto getUserProfile(@PathVariable Long id) {
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
