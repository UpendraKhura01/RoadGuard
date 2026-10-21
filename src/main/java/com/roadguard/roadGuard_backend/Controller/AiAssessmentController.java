package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.AIServices.AiAssessmentService;
import com.roadguard.roadGuard_backend.dto.AiAssessmentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/AiAssessment")
@RequiredArgsConstructor
public class AiAssessmentController {
    private final AiAssessmentService aiAssessmentService;

    @GetMapping("/getAiAssessment/{reportId}")
    public ResponseEntity<AiAssessmentResponseDto> getAiAssessment(@PathVariable Long reportId) {
        return ResponseEntity.ok(aiAssessmentService.getAiAssessment(reportId));
    }
}
