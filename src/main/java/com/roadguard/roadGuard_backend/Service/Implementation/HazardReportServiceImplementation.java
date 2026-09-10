package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.HazardReportRepository;
import com.roadguard.roadGuard_backend.Repository.ReportImageRepository;
import com.roadguard.roadGuard_backend.Repository.ReportSecurityCheckRepository;
import com.roadguard.roadGuard_backend.Repository.UserRepository;
import com.roadguard.roadGuard_backend.Service.*;
import com.roadguard.roadGuard_backend.Service.AIServices.AiAssessmentService;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.dto.LocationDetailsDto;
import com.roadguard.roadGuard_backend.entity.*;
import com.roadguard.roadGuard_backend.entity.types.HazardCategory;
import com.roadguard.roadGuard_backend.entity.types.ImageSource;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class HazardReportServiceImplementation implements HazardReportService {

    private final ImageStoringService imageStoringService;
    private final ReportSecurityCheckService reportSecurityCheckService;
    private final UserRepository userRepository;
    private final ReverseLocationService reverseLocationService;
    private final HazardReportRepository hazardReportRepository;
    private final ReportImageRepository reportImageRepository;
    private final ReportSecurityCheckRepository reportSecurityCheckRepository;
    private final AiAssessmentService aiAssessmentService;
    private final HazardReportUtil hazardReportUtil;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    @Transactional
    public HazardReportResponseDto createReport(Long userid,
                                                CreateHazardRequestDto request,
                                                MultipartFile photo){
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("User not found with user id :" + userid));

        ReportStatus initialStatus = reportSecurityCheckService.decideInitialStatus(request);
        LocationDetailsDto locationsDetailsDto = reverseLocationService.resolveLocation(request);

        HazardReport hazardReport = HazardReport.builder()
                .reportStatus(initialStatus)
                .description(request.getDescription())
                .user(user)
                .priorityScore(CalculateInitialScore(request.getReportedCategory(), initialStatus))
                .reportedCategory(request.getReportedCategory())
                .address(locationsDetailsDto.getAddress())
                .ward(locationsDetailsDto.getWard())
                .city(locationsDetailsDto.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .createdAt(request.getCapturedAt())
                .gpsAccuracyMeters(request.getGpsAccuracy())
                .build();

        HazardReport savedReport = hazardReportRepository.save(hazardReport);

        String imageUrl = imageStoringService.storeReportImage(photo);
        ReportImage image;
        image = ReportImage.builder()
                .hazardReport(savedReport)
                .url(imageUrl)
                .imageSource(ImageSource.CAMERA)
                .contentType(photo.getContentType())
                .fileSize(photo.getSize())
                .build();
        reportImageRepository.save(image);

        ReportSecurityCheck reportSecurityCheck = reportSecurityCheckService.buildSecurityCheck(savedReport, request);
        reportSecurityCheckRepository.save(reportSecurityCheck);

        ReportStatus beforeAi = savedReport.getReportStatus();

        AiAssessment aiAssessment = aiAssessmentService.assessReport(savedReport, request, photo);

        hazardReportUtil.updateReportWithAiAssessment(savedReport, aiAssessment);

        savedReport.setPriorityScore(aiAssessment.getAiPriorityScore());

        //update report with AiAssessment report, and it is in transactional context so it is atomic
        savedReport = hazardReportRepository.save(savedReport);
        if(beforeAi != aiAssessment.getHazardReport().getReportStatus()){
            hazardReportUtil.CreateHazardRequestHistory(savedReport,
                    beforeAi,
                    aiAssessment.getHazardReport().getReportStatus(),
                    user, aiAssessment.getExplanation());
        }
        notificationService.CreateReportSubmittedNotification(user, savedReport);

        HazardReportResponseDto responseDto = modelMapper.map(savedReport, HazardReportResponseDto.class);
        responseDto.setImageUrl(imageUrl);

        return responseDto;
    }

    private Integer CalculateInitialScore(HazardCategory reportedCategory, ReportStatus initialStatus) {

        if(initialStatus == ReportStatus.REJECTED || initialStatus == ReportStatus.AI_REJECTED || reportedCategory == null){
            return 0;
        }
        return switch (reportedCategory){
            case OPEN_MANHOLE -> 90;
            case BROKEN_TRAFFIC_SIGNAL -> 80;
            case FALLEN_TREE -> 75;
            case WATERLOGGING -> 65;
            case POTHOLE, DAMAGED_ROAD -> 60;
            case BROKEN_STREETLIGHT -> 40;
            case OTHER -> 30;
            case UNRELATED -> 0;
        };
    }
}
