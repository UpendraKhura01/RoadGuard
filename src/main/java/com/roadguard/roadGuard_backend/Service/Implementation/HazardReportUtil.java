package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.StatusHistoryRepository;
import com.roadguard.roadGuard_backend.entity.AiAssessment;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.StatusHistory;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.FakeLikeliHood;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HazardReportUtil {
    private final StatusHistoryRepository statusHistoryRepository;

    public void updateReportWithAiAssessment(HazardReport report, AiAssessment aiAssessment) {
        report.setAiSeverity(aiAssessment.getSeverity());
        report.setFinalCategory(aiAssessment.getHazardCategory());

        if(report.getFinalSeverity() == null){
            report.setFinalSeverity(aiAssessment.getSeverity());
        }
        if(report.getReportStatus() == ReportStatus.REJECTED){
            return;
        }
        FakeLikeliHood isFakeImage = aiAssessment.getFakeLikelihood();
        Boolean needsAdminReview = aiAssessment.getNeedsAdminReview();
        Boolean validHazard = aiAssessment.getValidHazard();
        Boolean descriptionMatches = aiAssessment.getDescriptionMatchesImage();

        if(isFakeImage == FakeLikeliHood.HIGH || !Boolean.TRUE.equals(validHazard)){
            report.setReportStatus(ReportStatus.AI_REJECTED);
            report.setPriorityScore(0);
        }
        else if(Boolean.TRUE.equals(needsAdminReview) || !Boolean.TRUE.equals(descriptionMatches)){
            report.setReportStatus(ReportStatus.PENDING_REVIEW);
        }
        else{
            report.setReportStatus(ReportStatus.SUBMITTED);
        }
    }

    public void CreateHazardRequestHistory(HazardReport report,
                                            ReportStatus lastStatus,
                                            ReportStatus newStatus,
                                            User user,
                                            String explanation){
        StatusHistory statusHistory = StatusHistory.builder()
                .hazardReport(report)
                .lastStatus(lastStatus)
                .newStatus(newStatus)
                .changedBy(user)
                .notes(explanation)
                .build();
        statusHistoryRepository.save(statusHistory);

    }

}
