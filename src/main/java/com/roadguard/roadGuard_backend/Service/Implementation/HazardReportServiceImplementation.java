package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.HazardReportRepository;
import com.roadguard.roadGuard_backend.Repository.ReportImageRepository;
import com.roadguard.roadGuard_backend.Repository.ReportSecurityCheckRepository;
import com.roadguard.roadGuard_backend.Repository.UserRepository;
import com.roadguard.roadGuard_backend.Security.AuthUtil;
import com.roadguard.roadGuard_backend.Service.*;
import com.roadguard.roadGuard_backend.Service.AIServices.AiAssessmentService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.*;
import com.roadguard.roadGuard_backend.entity.types.HazardCategory;
import com.roadguard.roadGuard_backend.entity.types.ImageSource;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private final AuthUtil authUtil;


    @Transactional
    public HazardReportResponseDto createReport(Long userId,
                                                CreateHazardRequestDto request,
                                                MultipartFile photo){

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        ReportStatus initialStatus = reportSecurityCheckService.decideInitialStatus(request);

        ResolveLocationDto req = ResolveLocationDto.builder()
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .build();
        LocationDetailsDto locationsDetailsDto = reverseLocationService.resolveLocation(req);

        ImageUploadResponseDto response = imageStoringService.storeReportImage(photo);
        ReportImage image;
        image = ReportImage.builder()
                .url(response.getUrl())
                .publicId(response.getPublicId())
                .imageSource(ImageSource.CAMERA)
                .contentType(photo.getContentType())
                .fileSize(photo.getSize())
                .build();
        ReportImage savedImage = reportImageRepository.save(image);

        HazardReport hazardReport = HazardReport.builder()
                .reportStatus(initialStatus)
                .reportImage(savedImage)
                .description(request.getDescription())
                .user(user)
                .priorityScore(CalculateInitialScore(request.getReportedCategory(), initialStatus))
                .reportedCategory(request.getReportedCategory())
                .road(locationsDetailsDto.getRoad())
                .ward(locationsDetailsDto.getWard())
                .block(locationsDetailsDto.getBlock())
                .district(locationsDetailsDto.getDistrict())
                .state(locationsDetailsDto.getState())
                .pinCode(locationsDetailsDto.getPinCode())
                .fullAddress(locationsDetailsDto.getFullAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .createdAt(request.getCapturedAt())
                .gpsAccuracyMeters(request.getGpsAccuracy())
                .build();

        HazardReport savedReport = hazardReportRepository.save(hazardReport);

        ReportSecurityCheck reportSecurityCheck = reportSecurityCheckService.buildSecurityCheck(savedReport, request);
        reportSecurityCheckRepository.save(reportSecurityCheck);
        savedReport.setReportSecurityCheck(reportSecurityCheck);

        ReportStatus beforeAi = savedReport.getReportStatus();

        AiAssessment aiAssessment = aiAssessmentService.assessReport(savedReport, request, photo);

        savedReport.setAiAssessment(aiAssessment);
        hazardReportUtil.updateReportWithAiAssessment(savedReport, aiAssessment);

        if (savedReport.getReportStatus() != ReportStatus.AI_REJECTED &&
                savedReport.getReportStatus() != ReportStatus.REJECTED) {
            savedReport.setPriorityScore(aiAssessment.getAiPriorityScore());
        }

        //update report with AiAssessment report, and it is in transactional context so it is atomic
        savedReport = hazardReportRepository.save(savedReport);
        if(beforeAi != savedReport.getReportStatus()){
            hazardReportUtil.CreateHazardRequestHistory(savedReport,
                    beforeAi,
                    savedReport.getReportStatus(),
                    user, aiAssessment.getExplanation());
        }
        notificationService.CreateReportSubmittedNotification(user, savedReport);

        HazardReportResponseDto responseDto = modelMapper.map(savedReport, HazardReportResponseDto.class);
        responseDto.setImageUrl(response.getUrl());

        return responseDto;
    }

    @Override
    public HazardReportResponseDto getReportById(Long id) {
        HazardReport report = hazardReportRepository.findById(id).orElse(null);
        if(report == null){
            throw new IllegalArgumentException("No report found with id " + id);
        }
        HazardReportResponseDto response = modelMapper.map(report, HazardReportResponseDto.class);
        return response;
    }

    @Override
    public List<HazardReportResponseDto> getMyReports(User user) {

        List<HazardReport> reports = hazardReportRepository.findByUser(user);

        List<HazardReportResponseDto> response = reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();

        return response;
    }

    @Override
    public Page<HazardReportResponseDto> getVerifiedReports(User user, Pageable pageable) {
        Page<HazardReport> reports = hazardReportRepository.findByReportStatus(ReportStatus.VERIFIED, pageable);
        return reports.map(report -> modelMapper.map(report, HazardReportResponseDto.class));
    }

    @Override
    public Page<HazardReportResponseDto> getReportsByStatus(ReportStatus status, Pageable pageable) {
        Page<HazardReport> reports = hazardReportRepository.findByReportStatus(status, pageable);
        return reports.map(report -> modelMapper.map(report, HazardReportResponseDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HazardReportResponseDto> getAllReports(Pageable pageable) {
        Page<HazardReport> responseDtos = hazardReportRepository.findAll(pageable);
        return responseDtos.map(report -> modelMapper.map(report, HazardReportResponseDto.class));
    }

    @Override
    public List<HazardReportResponseDto> getNearbyReports(User user) {


        if(user.getWard() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<ReportStatus> excludedStatuses = List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED, ReportStatus.AI_REJECTED);
        List<HazardReport> reports = hazardReportRepository.findByWardAndReportStatusNotIn(user.getWard(), excludedStatuses);
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public List<HazardReportResponseDto> getCityReports(User user) {


        if(user.getBlock() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<ReportStatus> excludedStatuses = List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED, ReportStatus.AI_REJECTED);
        List<HazardReport> reports = hazardReportRepository.findByBlockAndReportStatusNotIn(user.getBlock(), excludedStatuses);
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public List<HazardReportResponseDto> getDistrictReports(User user) {

        if(user.getDistrict() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<HazardReport> reports = hazardReportRepository.findByDistrict(user.getDistrict());
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public List<HazardReportResponseDto> getStateReports(User user) {

        if(user.getState() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<HazardReport> reports = hazardReportRepository.findByState(user.getState());
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public long getNearbyReportsCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(user.getWard() == null){
            throw new IllegalArgumentException("Location is not set");
        }
        List<ReportStatus> excludedStatuses = List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED, ReportStatus.AI_REJECTED);
        return hazardReportRepository.countByWardAndReportStatusNotIn(user.getWard(), excludedStatuses);
    }

    @Override
    public long getCityReportsCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(user.getBlock() == null){
            throw new IllegalArgumentException("Location is not set");
        }
        List<ReportStatus> excludedStatuses = List.of(ReportStatus.RESOLVED, ReportStatus.REJECTED, ReportStatus.AI_REJECTED);
        return hazardReportRepository.countByBlockAndReportStatusNotIn(user.getBlock(), excludedStatuses);
    }

    @Override
    public long getDistrictReportsCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(user.getDistrict() == null){
            throw new IllegalArgumentException("Location is not set");
        }
        return hazardReportRepository.countByDistrict(user.getDistrict());
    }

    @Override
    public long getStateReportsCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(user.getState() == null){
            throw new IllegalArgumentException("Location is not set");
        }
        return hazardReportRepository.countByState(user.getState());
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
