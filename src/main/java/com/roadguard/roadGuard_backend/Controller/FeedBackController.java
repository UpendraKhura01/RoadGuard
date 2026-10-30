package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.FeedBackService;
import com.roadguard.roadGuard_backend.dto.ApiMessageResponseDto;
import com.roadguard.roadGuard_backend.dto.FeedBackRequestDto;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback")
public class FeedBackController {
    private final FeedBackService feedBackService;

    @PostMapping("/report/feedback/{reportId}")
    ResponseEntity<ApiMessageResponseDto> GiveFeedBack(@PathVariable Long reportId,
                                                       @RequestBody @Valid FeedBackRequestDto request,
                                                       Authentication authentication){
        User user = (User) authentication.getPrincipal();
        Long id = user.getId();
        feedBackService.saveFeedBack(request, id, reportId);

        return ResponseEntity.ok(new ApiMessageResponseDto("FeedBack Given Successfully"));
    }

}
