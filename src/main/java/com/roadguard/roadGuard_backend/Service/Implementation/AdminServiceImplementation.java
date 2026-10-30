package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.*;
import com.roadguard.roadGuard_backend.Service.AdminService;
import com.roadguard.roadGuard_backend.Service.NotificationService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.*;
import com.roadguard.roadGuard_backend.entity.types.NotificationType;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import com.roadguard.roadGuard_backend.entity.types.ReviewDecision;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImplementation implements AdminService {
    private final AdminReviewRepository adminReviewRepository;
    private final HazardReportRepository hazardReportRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ContractorRepository contractorRepository;
    @Transactional
    @Override
    public AdminReviewResponseDto reviewReport(Long reportId, AdminReviewRequestDto request, Authentication authentication) {

        HazardReport report = hazardReportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        User admin = (User) authentication.getPrincipal();

        AdminReview review = adminReviewRepository.findByHazardReportId(reportId).orElseGet(AdminReview::new);
        ReportStatus oldStatus = report.getReportStatus();
        ReportStatus newStatus = statusFromDecision(request.getFinalDecision());

        review.setHazardReport(report);
        review.setReviewedBy(admin);
        review.setReviewedAt(LocalDateTime.now());

        modelMapper.map(request, review);

        report.setReportStatus(newStatus);
        report.setFinalCategory(request.getFinalCategory());
        report.setFinalSeverity(request.getFinalSeverity());
        hazardReportRepository.save(report);

        AdminReview savedReview = adminReviewRepository.save(review);

        createStatusHistory(admin, report,oldStatus, newStatus, request.getComment());
        notificationService.createNotification(report.getUser(),
                "Report Status Updated",
                "The status of your report has been updated to " + newStatus,
                NotificationType.REPORT_STATUS_UPDATED);

        return modelMapper.map(review, AdminReviewResponseDto.class);
    }

    @Override
    @Transactional
    public void updateReport(Long adminId, Long reportId, UpdateReportStatusRequestDto updates) {
        HazardReport report = hazardReportRepository.findById(reportId).orElseThrow(() ->
                new RuntimeException("Report doesn't exist with that id" + reportId));

        User admin = userRepository.findById(adminId).orElseThrow(() ->
                new RuntimeException("Admin doesn't exist with that id" + adminId));

        ReportStatus oldStatus = report.getReportStatus();
        report.setReportStatus(updates.getNewStatus());
        hazardReportRepository.save(report);

        createStatusHistory(admin, report, oldStatus, updates.getNewStatus(), updates.getNotes());
        notificationService.createNotification(report.getUser(),
                "Report Status Updated",
                "The status of your report has been updated to " + updates.getNewStatus(),
                NotificationType.REPORT_STATUS_UPDATED);
    }

    @Override
    public CreateContractorResponseDto createContractor(CreateContractorRequestDto request) {

       Contractor contractor = contractorRepository.findByPhoneNumber(request.getPhoneNumber());
       if(contractor != null){
           throw new IllegalArgumentException("Contractor already exists with that phone number");
       }
        contractor = Contractor.builder()
                .area(request.getArea())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .specialization(request.getSpecialization())
                .build();
        Contractor saved = contractorRepository.save(contractor);
        CreateContractorResponseDto responseDto = new CreateContractorResponseDto();
        modelMapper.map(saved, responseDto);

        responseDto.setId(saved.getId());

        return responseDto;
    }

    @Override
    public void assignContractor(AssignContractorDto request) {
        User admin  = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Contractor contractor = contractorRepository.findById(request.getContractorId()).orElseThrow();
        HazardReport report = hazardReportRepository.findById(request.getReportId()).orElseThrow();

        report.setContractor(contractor);
        createStatusHistory(admin, report, report.getReportStatus(), ReportStatus.ASSIGNED, "Contractor Assigned " + contractor.getName());
        report.setReportStatus(ReportStatus.ASSIGNED);
        hazardReportRepository.save(report);

        notificationService.createNotification(report.getUser(),
                "Contractor Assigned",
                "A contractor has been assigned to your report",
                NotificationType.REPORT_CONTRACTOR_ASSIGNED);
    }

    @Override
    public List<ContractorResponseDto> getAllContractors() {
        List<Contractor> contractors = contractorRepository.findAll();
        return contractors.stream()
                .map(contractor -> modelMapper.map(contractor, ContractorResponseDto.class))
                .toList();
    }

    @Override
    public AnalyticsResponseDto getAnalytics() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);

        return AnalyticsResponseDto.builder()
                .totalReports(hazardReportRepository.count())
                .pendingReports(hazardReportRepository.countByReportStatus(ReportStatus.PENDING_REVIEW))
                .rejectedReports(hazardReportRepository.countByReportStatus(ReportStatus.REJECTED))
                .verifiedReports(hazardReportRepository.countByReportStatus(ReportStatus.VERIFIED))
                .totalUsers(userRepository.count())
                .activeContractors(contractorRepository.count())
                .reportsThisWeek(hazardReportRepository.countByCreatedAtAfter(weekAgo))
                .reportsThisMonth(hazardReportRepository.countByCreatedAtAfter(monthAgo))
                .build();
    }

    @Transactional
    private void createStatusHistory(User admin,
                                     HazardReport report,
                                     ReportStatus oldStatus,
                                     ReportStatus newStatus,
                                     String comment) {
        StatusHistory statusHistory = StatusHistory.builder()
                .hazardReport(report)
                .lastStatus(oldStatus)
                .newStatus(newStatus)
                .notes(comment)
                .changedBy(admin)
                .build();
        statusHistoryRepository.save(statusHistory);
    }

    ReportStatus statusFromDecision(ReviewDecision decision) {
        return switch (decision) {
            case APPROVE -> ReportStatus.VERIFIED;
            case REJECT -> ReportStatus.REJECTED;
            case NEED_MORE_INFO -> ReportStatus.PENDING_REVIEW;
            case MARK_DUPLICATE -> ReportStatus.DUPLICATE;
        };
    }
}
