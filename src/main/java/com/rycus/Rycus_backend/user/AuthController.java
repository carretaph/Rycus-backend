package com.rycus.Rycus_backend.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@CrossOrigin(
        origins = {
                "https://rycus.app",
                "https://www.rycus.app",
                "http://localhost:5173"
        },
        allowCredentials = "true"
)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ================================
    // REGISTER
    // ================================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {

        // Usa el nombre “efectivo”: primero name, si no fullName
        String effectiveName = request.getEffectiveName();

        User user = userService.registerUser(
                effectiveName,
                request.getEmail(),
                request.getPassword(),
                request.getPhone()   // 🔹 coincide con tu UserService
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
}
