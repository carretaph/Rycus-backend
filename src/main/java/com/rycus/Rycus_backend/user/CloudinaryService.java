package com.rycus.Rycus_backend.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class CloudinaryService {

    private final boolean enabled;
    private final Cloudinary cloudinary; // puede ser null si está disabled

    public CloudinaryService(
            @Value("${cloudinary.enabled:true}") boolean enabled
    ) {
        this.enabled = enabled;

        // ✅ Si está apagado (local), NO intentes configurar Cloudinary
        if (!enabled) {
            this.cloudinary = null;
            return;
        }

        // ✅ En prod (enabled=true), exige las variables de entorno
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        String apiKey = System.getenv("CLOUDINARY_API_KEY");
        String apiSecret = System.getenv("CLOUDINARY_API_SECRET");

        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloudinary is enabled but not configured (missing CLOUDINARY_* env vars)."
            );
        }

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String uploadImage(MultipartFile file, String folder) {
        // ✅ Local: si cloudinary.enabled=false → responde 503 controlado
        if (!enabled || cloudinary == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloudinary is disabled in this environment."
            );
        }

        try {
            Map<?, ?> res = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            Object secureUrl = res.get("secure_url");
            if (secureUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Cloudinary upload did not return a URL."
                );
            }

            return String.valueOf(secureUrl);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Avatar upload failed: " + e.getMessage()
            );
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
