package com.rycus.Rycus_backend.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ================================
    // REGISTRO
    // ================================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {

        // Usamos el nombre efectivo (name o fullName)
        String effectiveName = request.getEffectiveName();

        User user = userService.registerUser(
                effectiveName,
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                new AuthResponse("User registered successfully: " + user.getFullName())
        );
    }

    // ================================
    // LOGIN
    // ================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        User user = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                new AuthResponse("Login successful for: " + user.getFullName())
        );
    }

    // ================================
    // ✅ CHANGE EMAIL (NEW)
    // ================================
    @PostMapping("/change-email")
    public ResponseEntity<AuthResponse> changeEmail(@RequestBody ChangeEmailRequest req) {

        userService.changeEmail(
                req.getCurrentEmail(),
                req.getNewEmail(),
                req.getPassword()
        );

        return ResponseEntity.ok(new AuthResponse("Email updated successfully"));
    }
}
