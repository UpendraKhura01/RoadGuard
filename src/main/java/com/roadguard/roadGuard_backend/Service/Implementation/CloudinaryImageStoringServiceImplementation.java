package com.roadguard.roadGuard_backend.Service.Implementation;

import com.cloudinary.Cloudinary;
import com.roadguard.roadGuard_backend.Service.ImageStoringService;
import com.roadguard.roadGuard_backend.dto.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryImageStoringServiceImplementation implements ImageStoringService {

    private final Cloudinary cloudinary;



    public ImageUploadResponseDto storeReportImage(MultipartFile photo){
        validatePhoto(photo);
        try {
            String publicId = UUID.randomUUID().toString();

            Map data = cloudinary.uploader().upload(
                    photo.getBytes(),
                    Map.of(
                            "public_id", publicId,
                            "folder", "roadGuard/reportImage",
                            "resource_type", "auto"
                    ));
            String url = (String) data.get("secure_url");

            return ImageUploadResponseDto.builder()
                    .url(url)
                    .publicId(publicId)
                    .build();
        }
        catch (IOException exception){
            throw new RuntimeException("Couldn't Store uploaded image", exception);
        }
    }

    @Override
    public void deleteReportImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Collections.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("error in deleting the photo " + e);
        }
    }

    private void validatePhoto(MultipartFile photo) {

        if(photo == null || photo.isEmpty()){
            throw new IllegalArgumentException("Photo is empty");
        }
        String contentType = photo.getContentType();

        if(contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")){
            throw new IllegalArgumentException("photo type is invalid");
        }
    }

    private String getExtension(String originalFileName) {
        String cleanName = StringUtils.cleanPath(originalFileName == null ? "" : originalFileName);
        int dotIndex = cleanName.lastIndexOf(".");

        if (dotIndex == -1) {
            return ".jpg";
        }

        return cleanName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

}
