package com.rycus.Rycus_backend.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserAvatarController {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary; // puede ser null en local

    public UserAvatarController(UserRepository userRepository,
                                @Autowired(required = false) Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("email") String email,
            @RequestPart("file") MultipartFile file
    ) {
        // ✅ si en local no está configurado
        if (cloudinary == null) {
            return ResponseEntity.status(503).body("Cloudinary is not configured in this environment.");
        }

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("email is required");
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is required");
        }

        String contentType = (file.getContentType() == null)
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are allowed");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        try {
            String publicId = "user_" + user.getId();

            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "rycus/avatars",
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                return ResponseEntity.status(500).body("Cloudinary upload failed: missing secure_url");
            }

            String avatarUrl = secureUrl.toString().trim();
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            String fullName = (user.getFullName() != null && !user.getFullName().trim().isEmpty())
                    ? user.getFullName().trim()
                    : user.getEmail();

            return ResponseEntity.ok(new UserMiniDto(
                    user.getEmail(),
                    fullName,
                    user.getAvatarUrl()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
