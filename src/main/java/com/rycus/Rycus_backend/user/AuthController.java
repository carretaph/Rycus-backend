package com.rycus.Rycus_backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // REGISTRO
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {

        String name = request.getEffectiveName();

        User user = userService.registerUser(
                name,
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
        );

        AuthUserDto dto = AuthUserDto.from(user);

        return ResponseEntity.ok(
                new AuthResponse("Usuario registrado: " + user.getFullName(), dto)
        );
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            AuthUserDto dto = AuthUserDto.from(user);

            return ResponseEntity.ok(
                    new AuthResponse("Login correcto para: " + user.getFullName(), dto)
            );

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid email or password", null));
        }
    }
}
