package com.roadguard.roadGuard_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Contractor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Email
    private String email;

    @Column(nullable = false)
    private String area;

    @Column(nullable = false)
    private String specialization;

    @OneToMany(mappedBy = "contractor")
    private List<HazardReport> reports;
}
