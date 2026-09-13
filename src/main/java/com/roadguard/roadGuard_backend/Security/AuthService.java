package com.roadguard.roadGuard_backend.Security;

import com.roadguard.roadGuard_backend.Repository.UserRepository;
import com.roadguard.roadGuard_backend.Service.OtpService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.AuthProvider;
import com.roadguard.roadGuard_backend.entity.types.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;


    public SignupResponseDto signupPhoneNumber(SignupRequestDto request) {

        String number = request.getPhoneNumber();
        otpService.verifyOtp(request.getOtp(), number);

        User user  = userRepository.findByPhoneNumber(number).orElse(null);
        if(user != null){
            throw new BadCredentialsException("User already exists with the phone number :" + user);
        }

        user = User.builder()
                .name(request.getName())
                .authProvider(AuthProvider.PHONE)
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .roles(Roles.CITIZEN)
                .isVerified(false)
                .reputationScore(0L)
                .build();
        user = userRepository.save(user);

        String token = authUtil.generateToken(user);

        return SignupResponseDto.builder()
                .userId(user.getId())
                .username(user.getName())
                .message("User registered successfully")
                .build();

    }


}
