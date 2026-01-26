package com.rycus.Rycus_backend.user;

import com.rycus.Rycus_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserAvatarController {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public UserAvatarController(UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // POST /users/avatar?email=...
    // multipart/form-data: file
    @PostMapping("/avatar")
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @RequestParam("email") String email,
            @RequestParam("file") MultipartFile file
    ) {
        String e = email == null ? "" : email.trim().toLowerCase();
        if (e.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }

        User user = userRepository.findByEmail(e)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Subir a Cloudinary (debe devolver URL)
        String url = cloudinaryService.uploadImage(file, "avatars");

        user.setAvatarUrl(url);

        // safety por si usuarios viejos tienen null
        if (user.getPlanType() == null) {
            user.setPlanType(PlanType.FREE_TRIAL);
        }

        userRepository.save(user);

        return ResponseEntity.ok(new AvatarUploadResponse(url));
    }

    public static class AvatarUploadResponse {
        public String avatarUrl;
        public AvatarUploadResponse(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}
