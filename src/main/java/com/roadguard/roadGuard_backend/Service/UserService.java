package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.UpdatePasswordRequestDto;
import com.roadguard.roadGuard_backend.dto.UpdatePhoneNumberRequestDto;
import com.roadguard.roadGuard_backend.dto.UpdatePhoneNumberResponseDto;
import com.roadguard.roadGuard_backend.dto.UserProfileResponseDto;
import com.roadguard.roadGuard_backend.entity.User;


import java.util.Map;

public interface UserService {

    void updatePassword(UpdatePasswordRequestDto request);

    void updateLocation(User user, Double longitude, Double latitude);

     UserProfileResponseDto getProfile(Long id);

    void deleteAccount(Long id);

    void updateProfile(Long id, Map<String, Object> request);

    UpdatePhoneNumberResponseDto updatePhoneNumber(Long id, UpdatePhoneNumberRequestDto request);
}
