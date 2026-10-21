package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.UpdatePasswordRequestDto;
import com.roadguard.roadGuard_backend.dto.UpdateProfileRequestDto;
import com.roadguard.roadGuard_backend.dto.UserProfileResponseDto;
import org.springframework.security.core.Authentication;

public interface UserService {

    void updatePassword(UpdatePasswordRequestDto request);

    void updateLocation(Authentication authentication, Double longitude, Double latitude);

     UserProfileResponseDto getProfile(Authentication authentication);

    void deleteAccount(Authentication authentication);

    void updateProfile(Authentication authentication, UpdateProfileRequestDto request);
}
