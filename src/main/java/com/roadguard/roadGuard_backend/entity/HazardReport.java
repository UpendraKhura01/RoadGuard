package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.entity.types.HazardCategory;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import com.roadguard.roadGuard_backend.entity.types.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hazard_report", indexes = {
        @Index(name = "idx_report_status", columnList = "report_status"),
        @Index(name = "idx_ward", columnList = "ward"),
        @Index(name = "idx_block", columnList = "block"),
        @Index(name = "idx_district", columnList = "district"),
        @Index(name = "idx_state", columnList = "state"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_ward_status", columnList = "ward, report_status"),
        @Index(name = "idx_block_status", columnList = "block, report_status")
})
public class HazardReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(nullable = false)
    private ReportImage reportImage;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn
    private FeedBack feedBack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Contractor contractor;

    private String description;

    @Enumerated(EnumType.STRING)
    private HazardCategory reportedCategory;

    @Enumerated(EnumType.STRING)
    private HazardCategory finalCategory;

    @Enumerated(EnumType.STRING)
    private Severity aiSeverity;

    @Enumerated(EnumType.STRING)
    private Severity finalSeverity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus reportStatus;

    @OneToMany(mappedBy = "hazardReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatusHistory> statusHistoryList = new ArrayList<>();

    private Integer priorityScore;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;

    private Double gpsAccuracyMeters;


    //Location Details
    private String road;
    private String ward;
    private String block;
    private String district;
    private String state;
    private String fullAddress;
    private String pinCode;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn
    private ReportSecurityCheck reportSecurityCheck;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn
    private AiAssessment aiAssessment;

    private LocalDateTime createdAt;
    private LocalDateTime  updatedAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;

        if(reportStatus == null){
            reportStatus = ReportStatus.SUBMITTED;
        }
        if(priorityScore == null){
            priorityScore = 0;
        }
    }
    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }


    protected void onResolve(){
        resolvedAt = LocalDateTime.now();
    }
}
