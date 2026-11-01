package com.roadguard.roadGuard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyticsResponseDto {
    private long totalReports;
    private long pendingReports;
    private long verifiedReports;
    private long rejectedReports;
    private long totalUsers;
    private long activeContractors;
    private long reportsThisWeek;
    private long reportsThisMonth;
}
