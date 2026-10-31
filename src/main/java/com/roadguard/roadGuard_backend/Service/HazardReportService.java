package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HazardReportService {

    HazardReportResponseDto createReport(Long userid,
                                         CreateHazardRequestDto request,
                                         MultipartFile photo);


    HazardReportResponseDto getReportById(Long id);

    List<HazardReportResponseDto> getMyReports(User user);

    Page<@NonNull HazardReportResponseDto> getVerifiedReports(User user, Pageable pageable);

    Page<@NonNull HazardReportResponseDto> getReportsByStatus(ReportStatus status, Pageable pageable);

    Page<@NonNull HazardReportResponseDto> getAllReports(Pageable pageable);

    List<HazardReportResponseDto> getNearbyReports(User user);

    List<HazardReportResponseDto> getCityReports(User user);

    List<HazardReportResponseDto> getDistrictReports(User user);

    List<HazardReportResponseDto> getStateReports(User user);

    long getNearbyReportsCount(Long userId);

    long getCityReportsCount(Long userId);

    long getDistrictReportsCount(Long userId);

    long getStateReportsCount(Long userId);


}
