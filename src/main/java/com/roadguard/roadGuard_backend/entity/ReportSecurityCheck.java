package com.roadguard.roadGuard_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ReportSecurityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private HazardReport hazardReport;

    private Boolean cameraOnly;
    private Boolean fakeLocation;
    private Boolean vpnDetected;

    private String decisionReason;
}
