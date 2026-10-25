package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HazardReportRepository extends JpaRepository<HazardReport, Long> {
    List<HazardReport> findByUser(User user);

    List<HazardReport> findByReportStatus(ReportStatus reportStatus);

    List<HazardReport> findByWard(String ward);

    List<HazardReport> findByBlock(String block);

    List<HazardReport> findByDistrict(String district);

    List<HazardReport> findByState(String state);

    long countByReportStatus(ReportStatus reportStatus);

    long countByCreatedAtAfter(LocalDateTime weekAgo);
}