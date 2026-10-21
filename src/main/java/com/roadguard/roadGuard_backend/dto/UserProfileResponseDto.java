package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.entity.types.Roles;
import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponseDto {

    private String name;

    private String phoneNumber;

    private Roles role;

    private String reputationScore;

    private String address;

    @Email
    private String email;

}
