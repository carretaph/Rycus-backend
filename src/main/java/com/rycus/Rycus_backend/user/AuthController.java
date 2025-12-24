package com.rycus.Rycus_backend.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ============================
    // REGISTRO
    // ============================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {

        // Compatibilidad: toma name o fullName
        String effectiveName = request.getEffectiveName();

        User user = userService.registerUser(
                effectiveName,           // nombre completo
                request.getEmail(),      // email
                request.getPassword(),   // password
                request.getPhone()       // phone (puede venir null)
        );

        return ResponseEntity.ok(
                new AuthResponse("User registered successfully: " + user.getFullName())
        );
    }

    // ============================
    // LOGIN
    // ============================
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
}
