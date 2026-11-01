package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.Notification;
import com.roadguard.roadGuard_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
}