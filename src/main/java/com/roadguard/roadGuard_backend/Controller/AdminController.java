package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.AdminService;
import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/reports")
    public ResponseEntity<List<HazardReportResponseDto>> getReportsByStatus(@RequestParam ReportStatus status){
        List<HazardReportResponseDto> reports = hazardReportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/reviewReport")
    public ResponseEntity<AdminReviewResponseDto> reviewReport(@RequestParam Long reportId,
                                                                   @RequestBody AdminReviewRequestDto adminReviewRequestDto,
                                                                   Authentication authentication){
        AdminReviewResponseDto response = adminService.reviewReport(reportId, adminReviewRequestDto, authentication);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiMessageResponseDto> updateReport(Authentication authentication,
                                                              @RequestParam Long reportId,
                                                              @RequestBody UpdateReportStatusRequestDto updates){
        User admin = (User) authentication.getPrincipal();
        Long adminId = admin.getId();
        adminService.updateReport(adminId, reportId, updates);

        return ResponseEntity.ok(new ApiMessageResponseDto("Report Status Updated"));
    }
}
