package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Repository.HazardReportRepository;
import com.roadguard.roadGuard_backend.Security.AuthUtil;
import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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

    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HazardReportResponseDto> createReport(
            Authentication authentication,
            @Valid @RequestPart("data") CreateHazardRequestDto request,
            @RequestPart("photo") MultipartFile photo){
        if(photo == null || photo.isEmpty()){
            throw new IllegalArgumentException("Photo is mandatory:");
        }
        User user = authUtil.getAuthenticatedUser(authentication);
        Long userId = user.getId();
        HazardReportResponseDto response = hazardReportService.createReport(userId, request, photo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getReportById")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<HazardReportResponseDto> getReportById(@RequestParam Long id){
        HazardReportResponseDto response = hazardReportService.getReportById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/myReports")
    public ResponseEntity<List<HazardReportResponseDto>> getMyReports(Authentication authentication){
        List<HazardReportResponseDto> response = hazardReportService.getMyReports(authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verifiedReports")
    public ResponseEntity<List<HazardReportResponseDto>> getVerifiedReports(Authentication authentication){
        List<HazardReportResponseDto> verifiedReports = hazardReportService.getVerifiedReports(authentication);
        return ResponseEntity.ok(verifiedReports);
    }

    @GetMapping("/nearbyReports")
    public ResponseEntity<List<HazardReportResponseDto>> getNearByReports(Authentication authentication){
        List<HazardReportResponseDto> response = hazardReportService.getNearbyReports(authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cityReports")
    public ResponseEntity<List<HazardReportResponseDto>> getCityReports(Authentication authentication){
        List<HazardReportResponseDto> response = hazardReportService.getCityReports(authentication);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/DistrictReports")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<List<HazardReportResponseDto>> getDistrictReports(Authentication authentication){
        List<HazardReportResponseDto> response = hazardReportService.getDistrictReports(authentication);
        return ResponseEntity.ok(response);
    }




}
