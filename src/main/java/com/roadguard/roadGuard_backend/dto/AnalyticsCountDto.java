package com.roadguard.roadGuard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsCountDto {
    private Long totalReports;
    private Long pendingReports;
    private Long rejectedReports;
    private Long verifiedReports;
    private Long reportsThisWeek;
    private Long reportsThisMonth;
}
