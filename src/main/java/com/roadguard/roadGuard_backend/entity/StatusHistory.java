package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.types.ReportStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private HazardReport hazardReport;

    @Enumerated(EnumType.STRING)
    private ReportStatus lastStatus;

    @Enumerated(EnumType.STRING)
    private ReportStatus newStatus;

    @ManyToOne
    private User changeBy;

    private String notes;

    private LocalDate createdAt;
}
