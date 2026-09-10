package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}