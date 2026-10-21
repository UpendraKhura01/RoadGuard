package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.HazardReportRepository;
import com.roadguard.roadGuard_backend.Repository.ReportImageRepository;
import com.roadguard.roadGuard_backend.Repository.ReportSecurityCheckRepository;
import com.roadguard.roadGuard_backend.Repository.UserRepository;
import com.roadguard.roadGuard_backend.Security.AuthUtil;
import com.roadguard.roadGuard_backend.Service.*;
import com.roadguard.roadGuard_backend.Service.AIServices.AiAssessmentService;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.dto.LocationDetailsDto;
import com.roadguard.roadGuard_backend.dto.ResolveLocationDto;
import com.roadguard.roadGuard_backend.entity.*;
import com.roadguard.roadGuard_backend.entity.types.HazardCategory;
import com.roadguard.roadGuard_backend.entity.types.ImageSource;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HazardReportServiceImplementation implements HazardReportService {

    private final ImageStoringServiceImplementation imageStoringService;
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
    public HazardReportResponseDto createReport(Long userid,
                                                CreateHazardRequestDto request,
                                                MultipartFile photo){
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("User not found with user id :" + userid));

        ReportStatus initialStatus = reportSecurityCheckService.decideInitialStatus(request);

        ResolveLocationDto req = ResolveLocationDto.builder()
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .build();
        LocationDetailsDto locationsDetailsDto = reverseLocationService.resolveLocation(req);

        String imageUrl = imageStoringService.storeReportImage(photo);
        ReportImage image;
        image = ReportImage.builder()
                .url(imageUrl)
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

        savedReport.setPriorityScore(aiAssessment.getAiPriorityScore());

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
        responseDto.setImageUrl(imageUrl);

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
    public List<HazardReportResponseDto> getMyReports(Authentication authentication) {
        User user = authUtil.getAuthenticatedUser(authentication);
        List<HazardReport> reports = hazardReportRepository.findByUser(user);

        List<HazardReportResponseDto> response = reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();

        return response;
    }

    @Override
    public List<HazardReportResponseDto> getVerifiedReports(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<HazardReport> reports = hazardReportRepository.findByReportStatus(ReportStatus.VERIFIED);
        List<HazardReportResponseDto> response = reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
        return response;
    }

    @Override
    public List<HazardReportResponseDto> getReportsByStatus(ReportStatus status) {
        List<HazardReport> reports = hazardReportRepository.findByReportStatus(status);
        List<HazardReportResponseDto> response = reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HazardReportResponseDto> getAllReports() {
        List<HazardReport> responseDtos = hazardReportRepository.findAll();
        return responseDtos.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public List<HazardReportResponseDto> getNearbyReports(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if(user.getWard() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<HazardReport> reports = hazardReportRepository.findByWard(user.getWard());
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public List<HazardReportResponseDto> getCityReports(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if(user.getBlock() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<HazardReport> reports = hazardReportRepository.findByBlock(user.getBlock());
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public List<HazardReportResponseDto> getDistrictReports(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if(user.getDistrict() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<HazardReport> reports = hazardReportRepository.findByDistrict(user.getDistrict());
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
    }

    @Override
    public List<HazardReportResponseDto> getStateReports(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if(user.getState() == null){
            throw new IllegalArgumentException("Location is not set");
        }

        List<HazardReport> reports = hazardReportRepository.findByState(user.getState());
        return reports.stream()
                .map(report -> modelMapper.map(report, HazardReportResponseDto.class))
                .toList();
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
