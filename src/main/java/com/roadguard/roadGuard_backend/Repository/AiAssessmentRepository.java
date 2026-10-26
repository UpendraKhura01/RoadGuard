package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.AiAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiAssessmentRepository extends JpaRepository<AiAssessment, Long> {

}