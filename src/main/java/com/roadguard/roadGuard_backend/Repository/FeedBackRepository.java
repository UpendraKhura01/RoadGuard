package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.FeedBack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedBackRepository extends JpaRepository<FeedBack, Long> {
}