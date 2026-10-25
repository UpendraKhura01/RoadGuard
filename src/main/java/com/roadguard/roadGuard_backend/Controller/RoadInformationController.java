package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.RoadInfoService;
import com.roadguard.roadGuard_backend.dto.RoadInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roadInfo")
@RequiredArgsConstructor
@EnableMethodSecurity
public class RoadInformationController {
    private final RoadInfoService roadInfoService;

    @GetMapping("/getRoadInfo/{latitude}/{longitude}")
    public ResponseEntity<RoadInfoResponseDto> getRoadInfo(@PathVariable Double latitude,
                                                           @PathVariable Double longitude){
        return ResponseEntity.ok(roadInfoService.getNearByRoad(latitude, longitude));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    @GetMapping("/contractor/{id}")
    public ResponseEntity<List<RoadInfoResponseDto>> getAllRoadInfoOfContractor(@PathVariable Long id){

        return ResponseEntity.ok(roadInfoService.getAllRoadInfoOfContractor(id));
    }


}
