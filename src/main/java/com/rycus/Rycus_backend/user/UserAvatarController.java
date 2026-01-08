package com.rycus.Rycus_backend.user;

import com.cloudinary.Cloudinary;
import com.rycus.Rycus_backend.repository.UserRepository;
import com.rycus.Rycus_backend.user.dto.UserMiniDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;

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
    public ResponseEntity<UserMiniDto> uploadAvatar(
            @RequestParam("email") String email,
            @RequestPart("file") MultipartFile file
    ) {
        // ✅ Si Cloudinary no está configurado (local), no tumbamos el backend:
        if (cloudinary == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloudinary is not configured in this environment."
            );
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }

        String contentType = (file.getContentType() == null)
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);

        if (!contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            String publicId = "user_" + user.getId();

            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of(
                    "folder", "rycus/avatars",
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "image"
            ));

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Cloudinary upload failed (missing secure_url)"
                );
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

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed");
        }
    }
}
