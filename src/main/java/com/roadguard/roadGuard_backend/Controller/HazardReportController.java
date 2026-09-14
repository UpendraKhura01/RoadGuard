package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Security.AuthUtil;
import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/citizen/report")
@RequiredArgsConstructor
public class HazardReportController {
    private final HazardReportService hazardReportService;
    private final AuthUtil authUtil;

    public ResponseEntity<HazardReportResponseDto> createReport(
            Authentication authentication,
            @RequestPart("data") CreateHazardRequestDto request,
            @RequestPart("photo") MultipartFile photo){
        User user = authUtil.getAuthenticatedUser(authentication);
        Long userId = user.getId();
        HazardReportResponseDto response = hazardReportService.createReport(userId, request, photo);
        return ResponseEntity.ok(response);
    }
}
