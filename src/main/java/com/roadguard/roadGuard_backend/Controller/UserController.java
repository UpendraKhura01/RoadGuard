package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.UserService;
import com.roadguard.roadGuard_backend.dto.*;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/forgotPassword")
    public ResponseEntity<ApiMessageResponseDto> forgotPassword(@RequestBody @Valid UpdatePasswordRequestDto request){
        userService.updatePassword(request);
        return ResponseEntity.ok(new ApiMessageResponseDto("Password has been updated"));
    }

    @PostMapping("update_location")
    public ResponseEntity<ApiMessageResponseDto> updateLocation(Authentication authentication,
                                                                @RequestParam Double longitude,
                                                                @RequestParam Double latitude){

        User user = (User) authentication.getPrincipal();

        userService.updateLocation(user, longitude, latitude);

        return ResponseEntity.ok(new ApiMessageResponseDto("Location has been updated"));
    }

    @GetMapping("/getProfile")
    public ResponseEntity<UserProfileResponseDto> getProfile(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        assert user != null;

        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @GetMapping("/deleteAccount")
    public ResponseEntity<ApiMessageResponseDto> deleteAccount(Authentication authentication){
        User user = (User) authentication.getPrincipal();

        userService.deleteAccount(user.getId());
        return ResponseEntity.ok(new ApiMessageResponseDto("Account has been deleted"));
    }

    @PatchMapping("/updateProfile")
    public ResponseEntity<ApiMessageResponseDto> updateProfile(
            Authentication authentication,
            @RequestBody Map<String, Object> updates){
        User user = (User) authentication.getPrincipal();

        assert user != null;
        userService.updateProfile(user.getId(), updates);
        return ResponseEntity.ok(new ApiMessageResponseDto("Profile has been updated"));
    }

    @PostMapping("/updatePhoneNumber")
    public ResponseEntity<ApiMessageResponseDto> updatePhoneNumber(Authentication authentication,@Valid @RequestBody UpdatePhoneNumberRequestDto request){
        User user = (User) authentication.getPrincipal();
        userService.updatePhoneNumber(user.getId(), request);
        return ResponseEntity.ok(new ApiMessageResponseDto("Phone number has been updated"));
    }
}
