package com.roadguard.roadGuard_backend.entity;

import com.roadguard.roadGuard_backend.entity.types.ImageSource;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageSource imageSource;

    private String contentType;

    private Long fileSize;
    private Integer height;
    private Integer width;

    @NotBlank
    private String publicId;

    private LocalDateTime uploadedAt;
}
