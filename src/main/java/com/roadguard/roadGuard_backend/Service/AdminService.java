package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AdminService {

    AdminReviewResponseDto reviewReport(Long reportId, AdminReviewRequestDto adminReviewRequestDto, Authentication authentication);

    void updateReport(Long adminID, Long reportId, UpdateReportStatusRequestDto updates);

    CreateContractorResponseDto createContractor(CreateContractorRequestDto request);

    void assignContractor(AssignContractorDto request);

    List<ContractorResponseDto> getAllContractors();

    AnalyticsResponseDto getAnalytics();
}
