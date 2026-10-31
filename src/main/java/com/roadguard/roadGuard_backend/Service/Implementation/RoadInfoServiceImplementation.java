package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.RoadInformationRepository;
import com.roadguard.roadGuard_backend.Service.RoadInfoService;
import com.roadguard.roadGuard_backend.dto.RoadInfoResponseDto;
import com.roadguard.roadGuard_backend.entity.RoadInformation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadInfoServiceImplementation implements RoadInfoService {
    private final RoadInformationRepository roadInformationRepository;
    private final ModelMapper modelMapper;
    public static final Double RADIUS = 0.01;

    @Override
    public RoadInfoResponseDto getRoadInfo(Double latitude, Double longitude) {
        RoadInformation roadInformation = roadInformationRepository.findByLatitudeAndLongitude(latitude, longitude);
        if(roadInformation == null){
            throw new EntityNotFoundException("Road not found at this location. It may not be indexed yet.");
        }

        RoadInfoResponseDto response =  modelMapper.map(roadInformation, RoadInfoResponseDto.class);
        response.setContractorName(roadInformation.getContractor().getName());
        response.setContractorContact(roadInformation.getContractor().getPhoneNumber());
        response.setContractorEmail(roadInformation.getContractor().getEmail());

        return response;
    }

    @Override
    public List<RoadInfoResponseDto> getAllRoadInfoOfContractor(Long contractorId) {
        List<RoadInformation> roadInformations = roadInformationRepository.findByContractorIdWithContractor(contractorId);

        return roadInformations.stream()
                .map(road -> {
                    RoadInfoResponseDto response = modelMapper.map(road, RoadInfoResponseDto.class);
                    response.setContractorName(road.getContractor().getName());
                    response.setContractorContact(road.getContractor().getPhoneNumber());
                    response.setContractorEmail(road.getContractor().getEmail());
                    return response;
                })
                .collect(Collectors.toList());
    }



    @Override
    public RoadInfoResponseDto getNearByRoad(Double latitude, Double longitude) {
        List<RoadInformation> roads = roadInformationRepository.findNearby(latitude, longitude, RADIUS);
        if (roads.isEmpty()) {
            throw new EntityNotFoundException("No road found nearby. It may not be indexed yet.");
        }
        RoadInformation road = roads.getFirst();

        RoadInfoResponseDto response = modelMapper.map(road, RoadInfoResponseDto.class);
        response.setContractorName(road.getContractor().getName());
        response.setContractorContact(road.getContractor().getPhoneNumber());
        response.setContractorEmail(road.getContractor().getEmail());
        return response;
    }


}
