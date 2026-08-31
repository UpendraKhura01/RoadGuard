package com.roadguard.roadGuard_backend.entity;


import com.roadguard.roadGuard_backend.types.HazardCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AiAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "report_id", nullable = false)
    private HazardReport hazardReport;

    private Boolean validHazard;

    @Enumerated(EnumType.STRING)
    private HazardCategory hazardCategory;


    private Double confidence;

    private String explanation;
}
