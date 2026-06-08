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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(
            UserService userService,
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

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
                ref,
                request.getPhone(),
                request.getOffersReferralFee(),
                request.getReferralFeeType(),
                request.getReferralFeeValue(),
                request.getReferralFeeNotes()
        );

        try {
            emailService.sendWelcomeEmail(
                    user.getEmail(),
                    user.getFullName()
            );
        } catch (Exception ex) {
            System.out.println("WELCOME EMAIL FAILED FOR USER: " + user.getEmail());
            ex.printStackTrace();
        }

        return ResponseEntity.ok(
                new AuthResponse(
                        "User registered successfully: " + user.getFullName()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        String email = request.getEmail();
        String rawPass = request.getPassword();

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "email is required"
            );
        }

        if (rawPass == null || rawPass.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "password is required"
            );
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid credentials"
                        )
                );

        if (!passwordEncoder.matches(rawPass, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

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

    @PostMapping("/change-email")
    public ResponseEntity<AuthResponse> changeEmail(
            @RequestBody ChangeEmailRequest req
    ) {
        userService.changeEmail(
                req.getCurrentEmail(),
                req.getNewEmail(),
                req.getPassword()
        );

        return ResponseEntity.ok(
                new AuthResponse("Email updated successfully")
        );
    }

    @GetMapping("/subscription-status")
    public ResponseEntity<SubscriptionStatusResponse> subscriptionStatus(
            @RequestParam("email") String email
    ) {
        return ResponseEntity.ok(
                userService.getSubscriptionStatus(email)
        );
    }
}