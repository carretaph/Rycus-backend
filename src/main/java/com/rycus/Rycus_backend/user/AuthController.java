package com.rycus.Rycus_backend.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ============================
    // REGISTER
    // ============================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {

        // usamos el nombre efectivo (name o fullName)
        String fullName = request.getEffectiveName();

        User user = userService.registerUser(
                fullName,
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
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
