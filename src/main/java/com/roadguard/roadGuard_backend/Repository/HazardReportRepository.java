package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.HazardReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HazardReportRepository extends JpaRepository<HazardReport, Long> {
}