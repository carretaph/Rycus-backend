package com.rycus.Rycus_backend.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        // 1) Preferir CLOUDINARY_URL si existe
        String cloudinaryUrl = env("CLOUDINARY_URL");
        if (!cloudinaryUrl.isBlank()) {
            return new Cloudinary(cloudinaryUrl);
        }

        // 2) Si no, usar las 3 variables
        String cloudName = env("CLOUDINARY_CLOUD_NAME");
        String apiKey = env("CLOUDINARY_API_KEY");
        String apiSecret = env("CLOUDINARY_API_SECRET");

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Missing Cloudinary env vars. Need CLOUDINARY_URL OR (CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET)"
            );
        }

        Map<String, String> cfg = new HashMap<>();
        cfg.put("cloud_name", cloudName);
        cfg.put("api_key", apiKey);
        cfg.put("api_secret", apiSecret);

        return new Cloudinary(cfg);
    }

    private String env(String k) {
        String v = System.getenv(k);
        return v == null ? "" : v.trim();
    }
}