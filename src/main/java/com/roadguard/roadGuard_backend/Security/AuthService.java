package com.roadguard.roadGuard_backend.Security;

import com.roadguard.roadGuard_backend.Repository.UserRepository;
import com.roadguard.roadGuard_backend.Service.OtpService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.AuthProvider;
import com.roadguard.roadGuard_backend.entity.types.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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
                .token(token)
                .role(user.getRoles())
                .build();

    }

    public LoginResponseDto loginPhoneNumber(OtpLoginRequestDto request) {
        String number = request.getPhoneNumber();
        User user = userRepository.findByPhoneNumber(number).orElse(null);
        if(user == null){
            throw new BadCredentialsException("Number is not registered:" + number);
        }
        otpService.verifyOtp(request.getOtp(), request.getPhoneNumber());

        String token = authUtil.generateToken(user);

        return LoginResponseDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .token(token)
                .role(user.getRoles())
                .message("Login Successful")
                .build();
    }

    public LoginResponseDto passwordLogin(passwordLoginRequestDto request) {
        String number = request.getPhoneNumber();
        User user = userRepository.findByPhoneNumber(number).orElse(null);
        if(user == null){
            throw new BadCredentialsException("Number is not registered:" + number);
        }
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword()));
            user = (User) authentication.getPrincipal();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Wrong password");
        }

        String token = authUtil.generateToken(user);

        return LoginResponseDto.builder()
                .token(token)
                .phoneNumber(number)
                .name(user.getName())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRoles())
                .message("Login Successful")
                .build();
    }

    public ResponseEntity<?> oAuth2Login(OAuth2AuthenticationToken Authtoken) {
        OAuth2User oAuth2User = Authtoken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiMessageResponseDto("No user Exists with that Email"));
        }
        String jwtToken = authUtil.generateToken(user);
        LoginResponseDto response = LoginResponseDto.builder()
                .email(email)
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .token(jwtToken)
                .userId(user.getId())
                .role(user.getRoles())
                .build();

        return ResponseEntity.ok(response);
    }
}
