package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.types.AuthProvider;
import com.roadguard.roadGuard_backend.types.Roles;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JoinColumn(unique = true, nullable = false)
    private String gmail;

    private String phoneNum;

    @JoinColumn(nullable = false)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    private Roles roles;

    @JoinColumn(nullable = false)
    private Boolean isVerified;

    private Long reputationScore;

}
