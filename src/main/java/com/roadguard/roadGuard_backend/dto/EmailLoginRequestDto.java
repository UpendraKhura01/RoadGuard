package com.roadguard.roadGuard_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginRequestDto {

    @NotBlank(message = "Enter the Gmail")
    @Email
    private String email;

    @NotBlank(message = "Enter the Password")
    private String password;
}
