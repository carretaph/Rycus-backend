package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.security.JwtService;
import com.rycus.Rycus_backend.user.dto.SafeUserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // ================================
    // REGISTRO
    // ================================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody AuthRequest request,
            @RequestParam(value = "ref", required = false) String ref
    ) {
        String effectiveName = request.getEffectiveName();

        User user = userService.registerUser(
                effectiveName,
                request.getEmail(),
                request.getPassword(),
                ref
        );

        return ResponseEntity.ok(
                new AuthResponse("User registered successfully: " + user.getFullName())
        );
    }

    // ================================
    // LOGIN (✅ token + safe user DTO)
    // ================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        User user = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user.getEmail());

        SafeUserDto safeUser = SafeUserDto.from(user);

        return ResponseEntity.ok(
                new AuthResponse(
                        "Login successful for: " + safeUser.getFullName(),
                        token,
                        safeUser
                )
        );
    }

    // ================================
    // CHANGE EMAIL
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

    // ================================
    // Subscription status simple
    // ================================
    @GetMapping("/subscription-status")
    public ResponseEntity<SubscriptionStatusResponse> subscriptionStatus(
            @RequestParam("email") String email
    ) {
        return ResponseEntity.ok(userService.getSubscriptionStatus(email));
    }
}
