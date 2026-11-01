package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.ReportSecurityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportSecurityCheckRepository extends JpaRepository<ReportSecurityCheck, Long> {
}