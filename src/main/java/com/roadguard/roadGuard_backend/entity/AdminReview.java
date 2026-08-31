package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.types.HazardCategory;
import com.roadguard.roadGuard_backend.types.ReviewDecision;
import com.roadguard.roadGuard_backend.types.Severity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter
public class AdminReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "hazard_id")
    private HazardReport hazardReport;

    @ManyToOne
    private User reviewedBy;
    /**
     *   Here user refers to the admin
     *   One admin can have multiple review reports
     *   So it is not user but admin
     *   admin = user + role
     */

    @Enumerated(EnumType.STRING)
    private ReviewDecision finalDecision;

    @Enumerated(EnumType.STRING)
    private Severity finalSeverity;

    @Enumerated(EnumType.STRING)
    private HazardCategory finalCategory;

    private String comment;

    private LocalDate reviewedAt;

}
