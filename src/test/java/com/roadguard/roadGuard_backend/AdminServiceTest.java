package com.roadguard.roadGuard_backend;

import com.roadguard.roadGuard_backend.Repository.ContractorRepository;
import com.roadguard.roadGuard_backend.Service.AdminService;
import com.roadguard.roadGuard_backend.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ContractorRepository contractorRepository;

    @Test
    public void testCreateContractor() {
        CreateContractorRequestDto requestDto = CreateContractorRequestDto.builder()
                .name("Upendra")
                .area("Damanjodi")
                .phoneNumber("1236754756")
                .specialization("Road complaining")
                .build();
        CreateContractorResponseDto responseDto = adminService.createContractor(requestDto);
        System.out.println(responseDto);
    }

    @Test
    public void testGetAllContractors() {
        List<ContractorResponseDto> responseDtoList = adminService.getAllContractors();
        System.out.println(responseDtoList);
    }

    @Test
    public void testAssignContractor() {
        AssignContractorDto request = AssignContractorDto.builder()
                .reportId(7L)
                .contractorId(1L)
                .build();

        adminService.assignContractor(request);
        System.out.println("assigned");
    }
    @Test
    public void testGetAnalytics() {
        AnalyticsResponseDto responseDto = adminService.getAnalytics();
        System.out.println(responseDto);
    }
}
