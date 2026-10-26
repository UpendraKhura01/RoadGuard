package com.roadguard.roadGuard_backend;

import com.roadguard.roadGuard_backend.Service.UserService;
import com.roadguard.roadGuard_backend.dto.UserProfileResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Test
    public void getProfileTest(){

        UserProfileResponseDto userProfileResponseDto = userService.getProfile(1L);
        System.out.println(userProfileResponseDto);
    }

    @Test
    public void updateProfileTest(){
        Map<String, Object> request = Map.of("email", "dmjkillee@gmail.com");
        userService.updateProfile(1L, (Map<String, Object>) request);
    }


}
