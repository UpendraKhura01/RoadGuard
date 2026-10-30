package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.HazardReportRepository;
import com.roadguard.roadGuard_backend.dto.FeedBackRequestDto;
import com.roadguard.roadGuard_backend.Repository.FeedBackRepository;
import com.roadguard.roadGuard_backend.Service.FeedBackService;
import com.roadguard.roadGuard_backend.entity.FeedBack;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedBackServiceImplementation implements FeedBackService {
    private final FeedBackRepository feedBackRepository;
    private final HazardReportRepository hazardReportRepository;

    @Override
    @Transactional
    public void saveFeedBack(FeedBackRequestDto request, Long id, Long reportId) {

        HazardReport report = hazardReportRepository.findById(reportId).orElse(null);
        if(report == null){
            throw new RuntimeException("Report not found with id " + reportId);
        }
        // Check report belongs to user
        if(!report.getUser().getId().equals(id)){
            throw new AccessDeniedException("Unauthorized: You can only submit feedback for your own reports");
        }

        if(report.getReportStatus() != ReportStatus.RESOLVED){
            throw new IllegalArgumentException("FeedBack can only be given for RESOLVED reports");
        }
        if(report.getFeedBack() != null) {
            throw new RuntimeException("Feedback already given for this report");
        }
        FeedBack feedBack = FeedBack.builder()
                .feedback(request.getFeedback())
                .rating(request.getRating())
                .build();
        feedBackRepository.save(feedBack);

        report.setFeedBack(feedBack);
        hazardReportRepository.save(report);

    }
}
