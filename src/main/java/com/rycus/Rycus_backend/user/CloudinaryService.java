package com.rycus.Rycus_backend.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file, String folder) {
        try {
            String publicId = folder + "/" + UUID.randomUUID();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            "resource_type", "image",
                            "overwrite", true
                    )
            );

            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new RuntimeException("Cloudinary did not return secure_url");
            }

            return secureUrl.toString();

        } catch (Exception e) {
            // Propaga el error real
            throw new RuntimeException("Cloudinary upload error: " + e.getMessage(), e);
        }
    }
}