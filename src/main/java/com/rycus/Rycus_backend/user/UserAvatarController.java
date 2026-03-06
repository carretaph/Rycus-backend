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

    public UserAvatarController(UserRepository userRepository,
                                CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // =====================================================
    // POST /users/avatar?email=...
    // multipart/form-data: file
    // =====================================================
    @PostMapping("/avatar")
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @RequestParam("email") String email,
            @RequestParam("file") MultipartFile file
    ) {

        System.out.println("=== AVATAR UPLOAD START ===");

        String e = email == null ? "" : email.trim().toLowerCase();

        if (e.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "email is required"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "file is required"
            );
        }

        System.out.println("Email: " + e);
        System.out.println("Filename: " + file.getOriginalFilename());
        System.out.println("Size: " + file.getSize());

        User user = userRepository
                .findByEmailIgnoreCase(e)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        try {

            System.out.println("Uploading to Cloudinary...");

            String url = cloudinaryService.uploadImage(file, "avatars");

            System.out.println("Cloudinary URL: " + url);

            user.setAvatarUrl(url);

            if (user.getPlanType() == null) {
                user.setPlanType(PlanType.FREE_TRIAL);
            }

            userRepository.save(user);

            System.out.println("User saved with new avatar");
            System.out.println("=== AVATAR UPLOAD SUCCESS ===");

            return ResponseEntity.ok(new AvatarUploadResponse(url));

        } catch (Exception ex) {

            System.err.println("=== AVATAR UPLOAD FAILED ===");
            ex.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloudinary upload failed: " + ex.getMessage()
            );
        }
    }

    // =====================================================
    public static class AvatarUploadResponse {

        public String avatarUrl;

        public AvatarUploadResponse(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}