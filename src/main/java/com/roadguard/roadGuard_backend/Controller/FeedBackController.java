package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.FeedBackService;
import com.roadguard.roadGuard_backend.dto.ApiMessageResponseDto;
import com.roadguard.roadGuard_backend.dto.FeedBackRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback")
public class FeedBackController {
    private final FeedBackService feedBackService;

    @PostMapping("/report/feedback")
    ResponseEntity<ApiMessageResponseDto> GiveFeedBack(@RequestBody FeedBackRequestDto request, Authentication authentication){
        feedBackService.saveFeedBack(request, authentication);

        return ResponseEntity.ok(new ApiMessageResponseDto("FeedBack Given Successfully"));
    }

}
