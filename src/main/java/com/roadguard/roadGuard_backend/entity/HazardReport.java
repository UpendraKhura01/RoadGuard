package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.types.HazardCategory;
import com.roadguard.roadGuard_backend.types.ReportStatus;
import com.roadguard.roadGuard_backend.types.Severity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class HazardReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    //shouldn't this be report Id?
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String description;

    @Enumerated(EnumType.STRING)
    private HazardCategory reportedCategory;

    @Enumerated(EnumType.STRING)
    private HazardCategory finalCategory;

    @Enumerated(EnumType.STRING)
    private Severity aiSeverity;

    @Enumerated(EnumType.STRING)
    private Severity finalSeverity;

    private ReportStatus status;

    private Integer priorityScore;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;

    private Double gpsAccuracyMeters;

    private String address;
    private String city;
    private String ward;

    private LocalDate createdAt;
    private LocalDate updatedAt;



}
