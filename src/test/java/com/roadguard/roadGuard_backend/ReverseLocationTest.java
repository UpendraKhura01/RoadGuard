package com.roadguard.roadGuard_backend;

import com.roadguard.roadGuard_backend.Service.ReverseLocationService;
import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.LocationDetailsDto;
import com.roadguard.roadGuard_backend.dto.LocationsDetailsRootDto;
import com.roadguard.roadGuard_backend.dto.ResolveLocationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReverseLocationTest {

    @Autowired
    private ReverseLocationService reverseLocationService;

    @Test
    void resolveLocationTest() {
        ResolveLocationDto req = ResolveLocationDto.builder()
                .longitude(82.9153)
                .latitude(18.7419)
                .build();
        LocationDetailsDto locationsDetailsRootDto = reverseLocationService.resolveLocation(req);
        System.out.println(locationsDetailsRootDto);

    }

}
