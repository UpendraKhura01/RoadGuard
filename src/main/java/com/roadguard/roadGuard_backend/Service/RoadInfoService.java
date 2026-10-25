package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.RoadInfoResponseDto;

import java.util.List;

public interface RoadInfoService {
    RoadInfoResponseDto getRoadInfo(Double latitude, Double longitude);
    List<RoadInfoResponseDto> getAllRoadInfoOfContractor(Long id);

    RoadInfoResponseDto getNearByRoad(Double latitude, Double longitudes);
}
