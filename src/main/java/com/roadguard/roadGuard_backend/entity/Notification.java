package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.types.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**The user who receives the notification.**/
    @OneToOne
    private User user;

    /**Short message title**/
    private String title;

    private String message;

    /**For frontend to show different colour**/
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(nullable = false)
    private Boolean isRead;

    private LocalDate createdAt;
}
