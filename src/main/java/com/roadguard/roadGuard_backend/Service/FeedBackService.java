package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.FeedBackRequestDto;
import com.roadguard.roadGuard_backend.entity.User;

public interface FeedBackService {
    void saveFeedBack(FeedBackRequestDto feedBack, Long id, Long reportId);
}
