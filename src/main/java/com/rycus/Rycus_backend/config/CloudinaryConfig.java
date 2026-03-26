package com.rycus.Rycus_backend.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.enabled:false}")
    private boolean cloudinaryEnabled;

    @Bean
    public Cloudinary cloudinary() {
        if (!cloudinaryEnabled) {
            System.out.println("⚠️ Cloudinary disabled by property: cloudinary.enabled=false");
            return new Cloudinary(new HashMap<>());
        }

        // 1) Preferir CLOUDINARY_URL si existe
        String cloudinaryUrl = env("CLOUDINARY_URL");
        if (!cloudinaryUrl.isBlank()) {
            System.out.println("✅ Cloudinary configured with CLOUDINARY_URL");
            return new Cloudinary(cloudinaryUrl);
        }

        // 2) Si no, usar las 3 variables
        String cloudName = env("CLOUDINARY_CLOUD_NAME");
        String apiKey = env("CLOUDINARY_API_KEY");
        String apiSecret = env("CLOUDINARY_API_SECRET");

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            System.out.println("⚠️ Cloudinary env vars missing. Continuing with Cloudinary disabled.");
            return new Cloudinary(new HashMap<>());
        }

        Map<String, String> cfg = new HashMap<>();
        cfg.put("cloud_name", cloudName);
        cfg.put("api_key", apiKey);
        cfg.put("api_secret", apiSecret);

        System.out.println("✅ Cloudinary configured with cloud_name/api_key/api_secret");
        return new Cloudinary(cfg);
    }

    private String env(String k) {
        String v = System.getenv(k);
        return v == null ? "" : v.trim();
    }
}