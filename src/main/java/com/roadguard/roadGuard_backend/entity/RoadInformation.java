package com.roadguard.roadGuard_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RoadInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double latitude;

    private Double longitude;

    private BigDecimal budget;

    private LocalDateTime lastMaintenance;

    private LocalDateTime warrantyTill;

    @ManyToOne
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;

}
