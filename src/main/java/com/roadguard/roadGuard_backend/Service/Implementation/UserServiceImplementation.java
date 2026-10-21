package com.roadguard.roadGuard_backend.Service.Implementation;

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

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final ReverseLocationService reverseLocationService;
    private final ModelMapper modelMapper;

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
    public void updateLocation(Authentication authentication, Double longitude, Double latitude) {
        User user = (User) authentication.getPrincipal();

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
    public UserProfileResponseDto getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        UserProfileResponseDto response = modelMapper.map(user, UserProfileResponseDto.class);
        response.setAddress(user.getFullAddress());

        return response;
    }

    @Override
    public void deleteAccount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userRepository.delete(user);
    }

    @Override
    public void updateProfile(Authentication authentication, UpdateProfileRequestDto request) {
        User user = (User) authentication.getPrincipal();
        assert user != null;

        if(request.getName() != null && !request.getName().isEmpty()){
            user.setName(request.getName());
        }
        if(request.getEmail() != null && !request.getEmail().isEmpty()){
            user.setEmail(request.getEmail());
        }
        if(request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()){
            if (request.getOtp() == null || request.getOtp().isEmpty()) {
                throw new BadCredentialsException("OTP is required to update phone number");
            }
            otpService.verifyOtp(request.getOtp(), request.getPhoneNumber());
            user.setPhoneNumber(request.getPhoneNumber());

        }
        userRepository.save(user);
    }
}
