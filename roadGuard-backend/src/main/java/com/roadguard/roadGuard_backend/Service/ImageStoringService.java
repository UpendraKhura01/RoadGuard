package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.ImageUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStoringService {
    ImageUploadResponseDto storeReportImage(MultipartFile photo);
    void deleteReportImage(String publicId);

}
