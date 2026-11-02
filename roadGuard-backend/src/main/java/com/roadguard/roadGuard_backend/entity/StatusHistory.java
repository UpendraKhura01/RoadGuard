package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hazard_report_id", nullable = false)
    private HazardReport hazardReport;

    @Enumerated(EnumType.STRING)
    private ReportStatus lastStatus;

    @Enumerated(EnumType.STRING)
    private ReportStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;
}
