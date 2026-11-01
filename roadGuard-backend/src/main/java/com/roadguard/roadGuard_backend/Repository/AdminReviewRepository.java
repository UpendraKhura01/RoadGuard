package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.AdminReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminReviewRepository extends JpaRepository<AdminReview, Long> {
    Optional<AdminReview> findByHazardReportId(Long reportId);
}