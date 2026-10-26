package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Security.AuthUtil;
import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.Service.ImageValidationService;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@EnableMethodSecurity
@RequestMapping("/citizen")
@RequiredArgsConstructor
public class HazardReportController {
    private final HazardReportService hazardReportService;
    private final AuthUtil authUtil;
    private final ImageValidationService imageValidationService;

    @PostMapping(path = "/report/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HazardReportResponseDto> createReport(
            Authentication authentication,
            @Valid @RequestPart("data") CreateHazardRequestDto request,
            @RequestPart("photo") MultipartFile photo){

        imageValidationService.validateImage(photo);

        if(photo == null || photo.isEmpty()){
            throw new IllegalArgumentException("Photo is mandatory:");
        }
        User user = authUtil.getAuthenticatedUser(authentication);
        Long userId = user.getId();
        HazardReportResponseDto response = hazardReportService.createReport(userId, request, photo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getReportById/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<HazardReportResponseDto> getReportById(@PathVariable Long reportId){
        HazardReportResponseDto response = hazardReportService.getReportById(reportId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/myReports")
    public ResponseEntity<List<HazardReportResponseDto>> getMyReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getMyReports(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verifiedReports")
    public ResponseEntity<List<HazardReportResponseDto>> getVerifiedReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> verifiedReports = hazardReportService.getVerifiedReports(user);
        return ResponseEntity.ok(verifiedReports);
    }

    @GetMapping("/nearbyReports")
    public ResponseEntity<List<HazardReportResponseDto>> getNearByReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getNearbyReports(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cityReports")
    public ResponseEntity<List<HazardReportResponseDto>> getCityReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getCityReports(user);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/DistrictReports")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<List<HazardReportResponseDto>> getDistrictReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getDistrictReports(user);
        return ResponseEntity.ok(response);
    }




}
