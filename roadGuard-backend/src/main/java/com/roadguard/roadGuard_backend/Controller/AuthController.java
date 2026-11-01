package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Security.AuthService;
import com.roadguard.roadGuard_backend.Service.OAuthLoginTicketService;
import com.roadguard.roadGuard_backend.Service.OtpService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OtpService otpService;
    @Value("${AuthUrl}")
    String AuthUrl;

    @PostMapping("/sendOtp")
    public ResponseEntity<OtpResponse> sendOtp(@RequestBody @Valid OtpRequestDto requestDto) {
        OtpResponse otpResponse = otpService.sendOtp(requestDto.getPhoneNumber());
        return ResponseEntity.ok(otpResponse);
    }
    @PostMapping("/signup/phoneNumber")
    public ResponseEntity<SignupResponseDto> signupPhoneNumber(@RequestBody @Valid SignupRequestDto request){
        SignupResponseDto signupResponseDto = authService.signupPhoneNumber(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(signupResponseDto);
    }

    @PostMapping("/login/phoneNumber")
    public ResponseEntity<LoginResponseDto> otpLogin(@RequestBody @Valid OtpLoginRequestDto request){
        LoginResponseDto response = authService.loginPhoneNumber(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/password")
    public ResponseEntity<LoginResponseDto> passwordLogin(@RequestBody @Valid passwordLoginRequestDto request){
        LoginResponseDto response = authService.passwordLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth2/exchange")
    public ResponseEntity<LoginResponseDto> oauth2CodeExchange(@RequestBody @Valid OAuth2ExchangeRequestDto request){
        LoginResponseDto response = authService.oauth2CodeExchange(request.getCode());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oAuth2/mobileStart")
    public void startMobileOAuth2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().setAttribute("mobile-flow", true);
        response.sendRedirect(AuthUrl);

    }

}
