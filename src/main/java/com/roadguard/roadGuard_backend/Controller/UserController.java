package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.UserService;
import com.roadguard.roadGuard_backend.dto.ApiMessageResponseDto;
import com.roadguard.roadGuard_backend.dto.UpdatePasswordRequestDto;
import com.roadguard.roadGuard_backend.dto.UpdateProfileRequestDto;
import com.roadguard.roadGuard_backend.dto.UserProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/forgotPassword")
    public ResponseEntity<ApiMessageResponseDto> forgotPassword(UpdatePasswordRequestDto request){
        userService.updatePassword(request);
        return ResponseEntity.ok(new ApiMessageResponseDto("Password has been updated"));
    }

    @PostMapping("update_location")
    public ResponseEntity<ApiMessageResponseDto> updateLocation(Authentication authentication,
                                                                @RequestParam Double longitude,
                                                                @RequestParam Double latitude){

        userService.updateLocation(authentication, longitude, latitude);
        return ResponseEntity.ok(new ApiMessageResponseDto("Location has been updated"));
    }

    @GetMapping("/getProfile")
    public ResponseEntity<UserProfileResponseDto> getProfile(Authentication authentication){
        return ResponseEntity.ok(userService.getProfile(authentication));
    }

    @GetMapping("/deleteAccount")
    public ResponseEntity<ApiMessageResponseDto> deleteAccount(Authentication authentication){
        userService.deleteAccount(authentication);
        return ResponseEntity.ok(new ApiMessageResponseDto("Account has been deleted"));
    }

    @PatchMapping("/updateProfile")
    public ResponseEntity<ApiMessageResponseDto> updateProfile(Authentication authentication, UpdateProfileRequestDto request){
        userService.updateProfile(authentication, request);
        return ResponseEntity.ok(new ApiMessageResponseDto("Profile has been updated"));
    }

}
