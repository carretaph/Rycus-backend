package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.security.JwtService;
import com.rycus.Rycus_backend.user.dto.SafeUserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    // login directo DB
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserService userService,
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    // LOGIN (token + SafeUserDto con planType/subscriptionStatus)
    // ================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        String email = request.getEmail();      // tu AuthRequest ya lo normaliza (trim/lower)
        String rawPass = request.getPassword(); // trim

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }
        if (rawPass == null || rawPass.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(rawPass, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        // ✅ clave: devuelve planType y subscriptionStatus
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
