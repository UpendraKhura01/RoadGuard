package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.FeedBackRequestDto;
import org.springframework.security.core.Authentication;

public interface FeedBackService {
    void saveFeedBack(FeedBackRequestDto feedBack, Authentication authentication);
}
