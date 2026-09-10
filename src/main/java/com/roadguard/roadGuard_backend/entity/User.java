package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.entity.types.AuthProvider;
import com.roadguard.roadGuard_backend.entity.types.Roles;
import jakarta.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "App_User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String gmail;

    private String phoneNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Enumerated(EnumType.STRING)
    private Roles roles;

    @JoinColumn(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false)
    private Long reputationScore = 0L;

}
