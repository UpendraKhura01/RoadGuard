package com.roadguard.roadGuard_backend.Service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageValidationService {

    void validateImage(MultipartFile file);
}
