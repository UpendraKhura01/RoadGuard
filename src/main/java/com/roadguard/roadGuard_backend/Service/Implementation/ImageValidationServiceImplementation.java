package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Service.ImageValidationService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ImageValidationServiceImplementation implements ImageValidationService {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/jpg", "image/png");
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");

    @Override
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo is required");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
        }

        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("Only .jpg, .jpeg, and .png files are allowed");
            }
        }

    }
}
