package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.AdminService;
import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final HazardReportService hazardReportService;

    @GetMapping("/reports/byStatus")
    public ResponseEntity<@NonNull Page<@NonNull HazardReportResponseDto>> getReportsByStatus(
            @RequestParam ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir){

        if(size > 100) size = 100;
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<@NonNull HazardReportResponseDto> reports = hazardReportService.getReportsByStatus(status, pageable);
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/reviewReport/{reportId}")
    public ResponseEntity<@NonNull AdminReviewResponseDto> reviewReport(@PathVariable Long reportId,
                                                                   @RequestBody @Valid AdminReviewRequestDto adminReviewRequestDto,
                                                                   Authentication authentication){
        AdminReviewResponseDto response = adminService.reviewReport(reportId, adminReviewRequestDto, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateReportStatus/{reportId}")
    public ResponseEntity<ApiMessageResponseDto> updateReport(Authentication authentication,
                                                              @PathVariable Long reportId,
                                                              @RequestBody @Valid UpdateReportStatusRequestDto updates){
        User admin = (User) authentication.getPrincipal();
        Long adminId = admin.getId();
        adminService.updateReport(adminId, reportId, updates);

        return ResponseEntity.ok(new ApiMessageResponseDto("Report Status Updated"));
    }

    @PostMapping("/contractor/create")
    public ResponseEntity<CreateContractorResponseDto> createContractor(@RequestBody @Valid CreateContractorRequestDto request){
        CreateContractorResponseDto responseDto = adminService.createContractor(request);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/contractor/assign")
    public ResponseEntity<ApiMessageResponseDto> assignContractor(@RequestBody AssignContractorDto request){

        adminService.assignContractor(request);
        return ResponseEntity.ok(new ApiMessageResponseDto("Contractor Assigned"));
    }

    @GetMapping("/reports/all")
    public ResponseEntity<@NonNull Page<@NonNull HazardReportResponseDto>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir){

        if(size > 100) size = 100;
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<@NonNull HazardReportResponseDto> reports = hazardReportService.getAllReports(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/contractors")
    public ResponseEntity<List<ContractorResponseDto>> getAllContractors(){
        List<ContractorResponseDto> resposne = adminService.getAllContractors();
        return ResponseEntity.ok(resposne);
    }

    @GetMapping("/StateReports")
    public ResponseEntity<List<HazardReportResponseDto>> getStateReports(Authentication authentication){

        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(hazardReportService.getStateReports(user));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponseDto> getAnalytics(){
        AnalyticsResponseDto responseDto = adminService.getAnalytics();
        return ResponseEntity.ok(responseDto);
    }

}
