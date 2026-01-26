package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserMeController {

    private final UserRepository userRepository;

    public UserMeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET /users/me?email=...
    // ✅ Compat: /users/me?userEmail=...
    @GetMapping("/me")
    public ResponseEntity<UserMeDto> me(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "userEmail", required = false) String userEmail
    ) {
        String e = pickEmail(email, userEmail);

        if (e.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        User u = userRepository.findByEmail(e)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserMeDto dto = new UserMeDto();
        dto.id = u.getId();
        dto.email = u.getEmail();
        dto.fullName = u.getFullName();
        dto.avatarUrl = u.getAvatarUrl();
        dto.phone = u.getPhone();
        dto.businessName = u.getBusinessName();
        dto.city = u.getCity();
        dto.state = u.getState();

        return ResponseEntity.ok(dto);
    }

    private String pickEmail(String email, String userEmail) {
        String e1 = email == null ? "" : email.trim().toLowerCase();
        if (!e1.isEmpty() && !"null".equals(e1) && !"undefined".equals(e1)) return e1;

        String e2 = userEmail == null ? "" : userEmail.trim().toLowerCase();
        if (!e2.isEmpty() && !"null".equals(e2) && !"undefined".equals(e2)) return e2;

        return "";
    }

    public static class UserMeDto {
        public Long id;
        public String email;
        public String fullName;
        public String avatarUrl;
        public String phone;
        public String businessName;
        public String city;
        public String state;
    }
}
