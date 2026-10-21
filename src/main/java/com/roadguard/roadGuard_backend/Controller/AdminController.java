package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.AdminService;
import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.Contractor;
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

    @GetMapping("/reports/byStatus")
    public ResponseEntity<List<HazardReportResponseDto>> getReportsByStatus(@RequestParam ReportStatus status){
        List<HazardReportResponseDto> reports = hazardReportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/reviewReport/{reportId}")
    public ResponseEntity<AdminReviewResponseDto> reviewReport(@PathVariable Long reportId,
                                                                   @RequestBody AdminReviewRequestDto adminReviewRequestDto,
                                                                   Authentication authentication){
        AdminReviewResponseDto response = adminService.reviewReport(reportId, adminReviewRequestDto, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateReportStatus/{reportId}")
    public ResponseEntity<ApiMessageResponseDto> updateReport(Authentication authentication,
                                                              @PathVariable Long reportId,
                                                              @RequestBody UpdateReportStatusRequestDto updates){
        User admin = (User) authentication.getPrincipal();
        Long adminId = admin.getId();
        adminService.updateReport(adminId, reportId, updates);

        return ResponseEntity.ok(new ApiMessageResponseDto("Report Status Updated"));
    }

    @PostMapping("/contractor/create")
    public ResponseEntity<CreateContractorResponseDto> createContractor(@RequestBody CreateContractorRequestDto request){
        CreateContractorResponseDto responseDto = adminService.createContractor(request);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/contractor/assign")
    public ResponseEntity<ApiMessageResponseDto> assignContractor(@RequestBody AssignContractorDto request){

        adminService.assignContractor(request);
        return ResponseEntity.ok(new ApiMessageResponseDto("Contractor Assigned"));
    }

    @GetMapping("/reports/all")
    public ResponseEntity<List<HazardReportResponseDto>> getAllReports(){
        List<HazardReportResponseDto> reports = hazardReportService.getAllReports();
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

}
