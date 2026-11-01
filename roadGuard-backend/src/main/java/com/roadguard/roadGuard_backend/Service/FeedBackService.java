package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.FeedBackRequestDto;

public interface FeedBackService {
    void saveFeedBack(FeedBackRequestDto feedBack, Long id, Long reportId);
}
