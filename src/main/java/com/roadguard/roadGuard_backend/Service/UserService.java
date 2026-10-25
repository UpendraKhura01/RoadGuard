package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.UpdatePasswordRequestDto;
import com.roadguard.roadGuard_backend.dto.UpdatePhoneNumberRequestDto;
import com.roadguard.roadGuard_backend.dto.UpdateProfileRequestDto;
import com.roadguard.roadGuard_backend.dto.UserProfileResponseDto;
import com.roadguard.roadGuard_backend.entity.User;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface UserService {

    void updatePassword(UpdatePasswordRequestDto request);

    void updateLocation(User user, Double longitude, Double latitude);

     UserProfileResponseDto getProfile(Long id);

    void deleteAccount(Long id);

    void updateProfile(Long id, Map<String, Object> request);

    void updatePhoneNumber(Long id, UpdatePhoneNumberRequestDto request);
}
