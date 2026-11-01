package com.roadguard.roadGuard_backend.Service.AIServices;

import com.roadguard.roadGuard_backend.Repository.AiAssessmentRepository;
import com.roadguard.roadGuard_backend.Repository.HazardReportRepository;
import com.roadguard.roadGuard_backend.dto.AiAssessmentResponseDto;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.DirectAiResponseDto;
import com.roadguard.roadGuard_backend.entity.AiAssessment;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class AiAssessmentServiceImplementation implements  AiAssessmentService{

    private final AiUtil aiUtil;
    private final AiAssessmentRepository aiAssessmentRepository;
    private final ModelMapper modelMapper;
    private final HazardReportRepository hazardReportRepository;

    @Override
    @Transactional
    public AiAssessment assessReport(HazardReport report, CreateHazardRequestDto request, MultipartFile photo) {

        DirectAiResponseDto response = aiUtil.AiAssessReportWithProvider(request, photo);

        AiAssessment assessment = report.getAiAssessment();

        AiAssessment aiAssessment = AiAssessment.builder()
                .provider(response.getProvider())
                .isRoadImage(response.getIsRoadImage())
                .hazardCategory(response.getHazardCategory())
                .validHazard(response.getValidHazard())
                .confidence(response.getAiConfidence())
                .severity(response.getSeverity())
                .aiPriorityScore(response.getAiPriorityScore())
                .descriptionMatchesImage(response.getDescriptionMatchesImage())
                .needsAdminReview(response.getNeedsAdminReview())
                .fakeLikelihood(response.getFakeLikelihood())
                .explanation(response.getExplanation())
                .rawResponse(response.getRawResponse())
                .build();

        return aiAssessmentRepository.save(aiAssessment);

    }

    @Override
    public AiAssessmentResponseDto getAiAssessment(Long reportId) {
        HazardReport report = hazardReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID " + reportId));

        AiAssessment aiAssessment = report.getAiAssessment();
        if (aiAssessment == null){
            throw new IllegalArgumentException("AiAssessment not found");
        }

        AiAssessmentResponseDto responseDto = modelMapper.map(aiAssessment, AiAssessmentResponseDto.class);
        responseDto.setAssessmentId(aiAssessment.getId());
        return responseDto;
    }

}
