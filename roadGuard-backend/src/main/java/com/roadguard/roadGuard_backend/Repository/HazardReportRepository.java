package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.dto.AnalyticsCountDto;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HazardReportRepository extends JpaRepository<HazardReport, Long> {
    List<HazardReport> findByUser(User user);

    Page<HazardReport> findByReportStatus(ReportStatus reportStatus, Pageable pageable);

    List<HazardReport> findByWard(String ward);

    List<HazardReport> findByBlock(String block);

    List<HazardReport> findByDistrict(String district);

    List<HazardReport> findByState(String state);

    long countByReportStatus(ReportStatus reportStatus);

    long countByCreatedAtAfter(LocalDateTime weekAgo);

    List<HazardReport> findByWardAndReportStatusNotIn(String ward, List<ReportStatus> statuses);
    List<HazardReport> findByBlockAndReportStatusNotIn(String block, List<ReportStatus> statuses);

    // Count methods for filtered queries
    long countByWardAndReportStatusNotIn(String ward, List<ReportStatus> statuses);
    long countByBlockAndReportStatusNotIn(String block, List<ReportStatus> statuses);

    // Count methods for other areas (if needed)
    long countByDistrict(String district);
    long countByState(String state);

    @Query("SELECT new com.roadguard.roadGuard_backend.dto.AnalyticsCountDto(" +
            "COUNT(h), " +
            "SUM(CASE WHEN h.reportStatus = 'PENDING_REVIEW' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.reportStatus = 'REJECTED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.reportStatus = 'VERIFIED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.createdAt >= :weekAgo THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN h.createdAt >= :monthAgo THEN 1 ELSE 0 END)" +
            ") FROM HazardReport h")
    AnalyticsCountDto getAnalyticsCounts(@Param("weekAgo") LocalDateTime weekAgo,
                                         @Param("monthAgo") LocalDateTime monthAgo);
}