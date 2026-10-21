package com.roadguard.roadGuard_backend.Service.Implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadguard.roadGuard_backend.Repository.UserRepository;
import com.roadguard.roadGuard_backend.Service.OtpService;
import com.roadguard.roadGuard_backend.Service.ReverseLocationService;
import com.roadguard.roadGuard_backend.Service.UserService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final ReverseLocationService reverseLocationService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequestDto request) {
        String phoneNumber = request.getPhoneNumber();
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if(user == null){
            throw new BadCredentialsException("User does not exist with that phone number:");
        }

        String otp = request.getOtp();

        otpService.verifyOtp(otp, phoneNumber);
        String newPassword = request.getNewPassword();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateLocation(User user, Double longitude, Double latitude) {

        user.setLongitude(longitude);
        user.setLatitude(latitude);

        ResolveLocationDto req = ResolveLocationDto.builder()
                .longitude(longitude)
                .latitude(latitude)
                .build();

        LocationDetailsDto locationDetails = reverseLocationService.resolveLocation(req);

        modelMapper.map(locationDetails, user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDto getProfile(Long id) {

        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            throw new BadCredentialsException("User does not exist with that id:");
        }
        UserProfileResponseDto response = modelMapper.map(user, UserProfileResponseDto.class);
        response.setAddress(user.getFullAddress());

        return response;
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            throw new BadCredentialsException("User does not exist with that id:");
        }
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void updateProfile(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            throw new BadCredentialsException("User does not exist with that id:");
        }
        User updatedUser = objectMapper.convertValue(updates, User.class);
        modelMapper.map(updatedUser, user);
        userRepository.save(user);
    }


}
