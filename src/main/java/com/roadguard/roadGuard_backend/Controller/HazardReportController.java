package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Security.AuthUtil;
import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.Service.ImageValidationService;
import com.roadguard.roadGuard_backend.dto.CitizenReportResponseDto;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/citizen")
@RequiredArgsConstructor
public class HazardReportController {
    private final HazardReportService hazardReportService;
    private final AuthUtil authUtil;
    private final ImageValidationService imageValidationService;
    private final ModelMapper modelMapper;

    @PreAuthorize("hasAnyRole('CITIZEN')")
    @PostMapping(path = "/report/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<@NonNull HazardReportResponseDto> createReport(
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
    public ResponseEntity<@NonNull HazardReportResponseDto> getReportById(@PathVariable Long reportId){
        HazardReportResponseDto response = hazardReportService.getReportById(reportId);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CITIZEN')")
    @GetMapping("/myReports")
    public ResponseEntity<@NonNull List<@NonNull CitizenReportResponseDto>> getMyReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getMyReports(user);
        List<CitizenReportResponseDto> citizenReports = response.stream()
                .map(report -> modelMapper.map(report, CitizenReportResponseDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(citizenReports);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @GetMapping("/admin/verifiedReports")
    public ResponseEntity<@NonNull Page<@NonNull HazardReportResponseDto>> getVerifiedReports(Authentication authentication,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                            @RequestParam(defaultValue = "desc") String sortDir){
        if(size > 100) size = 100;
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        User user = (User)authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<@NonNull HazardReportResponseDto> verifiedReports = hazardReportService.getVerifiedReports(user, pageable);
        return ResponseEntity.ok(verifiedReports);
    }

    @PreAuthorize("hasAnyRole('CITIZEN')")
    @GetMapping("/verifiedReports")
    public ResponseEntity<@NonNull Page<@NonNull CitizenReportResponseDto>> getVerifiedReportsCitizen(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        User user = (User)authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<@NonNull HazardReportResponseDto> verifiedReports = hazardReportService.getVerifiedReports(user, pageable);
        Page<@NonNull CitizenReportResponseDto> reports = verifiedReports.map(report -> modelMapper.map(report, CitizenReportResponseDto.class));
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @GetMapping("/admin/nearbyReports")
    public ResponseEntity<@NonNull List<HazardReportResponseDto>> getNearByReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getNearbyReports(user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CITIZEN')")
    @GetMapping("/nearbyReports")
    public ResponseEntity<@NonNull List<CitizenReportResponseDto>> getNearByReportsCitizen(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getNearbyReports(user);
        List<CitizenReportResponseDto> reports = response.stream()
                .map(report -> modelMapper.map(report, CitizenReportResponseDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @GetMapping("/admin/cityReports")
    public ResponseEntity<List<HazardReportResponseDto>> getCityReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getCityReports(user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('CITIZEN')")
    @GetMapping("/cityReports")
    public ResponseEntity<List<CitizenReportResponseDto>> getCityReportsCitizen(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getCityReports(user);
        List<CitizenReportResponseDto> reports = response.stream()
                .map(report -> modelMapper.map(report, CitizenReportResponseDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reports);

    }
    @GetMapping("/DistrictReports")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<List<HazardReportResponseDto>> getDistrictReports(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        List<HazardReportResponseDto> response = hazardReportService.getDistrictReports(user);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/nearbyReports/count")
    public ResponseEntity<Long> getNearByReportsCount(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        return ResponseEntity.ok(hazardReportService.getNearbyReportsCount(user.getId()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @GetMapping("/cityReports/count")
    public ResponseEntity<Long> getCityReportsCount(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        return ResponseEntity.ok(hazardReportService.getCityReportsCount(user.getId()));
    }

    @GetMapping("/DistrictReports/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<Long> getDistrictReportsCount(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        return ResponseEntity.ok(hazardReportService.getDistrictReportsCount(user.getId()));
    }

    @GetMapping("/StateReports/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<Long> getStateReportsCount(Authentication authentication){
        User user = (User)authentication.getPrincipal();
        return ResponseEntity.ok(hazardReportService.getStateReportsCount(user.getId()));
    }


}
