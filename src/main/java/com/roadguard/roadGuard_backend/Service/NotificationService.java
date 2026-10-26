package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.NotificationResponseDto;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.Notification;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.NotificationType;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface NotificationService {

    Notification createNotification(User user, String title, String message, NotificationType notificationType);

    Notification CreateReportSubmittedNotification(User user, HazardReport report);

     List<NotificationResponseDto> getNotifications(Authentication authentication);

     NotificationResponseDto markAsRead(Authentication authentication, Long notificationId);
}
