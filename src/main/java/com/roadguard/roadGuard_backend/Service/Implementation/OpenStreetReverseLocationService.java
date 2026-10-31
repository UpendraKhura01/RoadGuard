package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Service.ReverseLocationService;
import com.roadguard.roadGuard_backend.dto.LocationDetailsDto;
import com.roadguard.roadGuard_backend.dto.LocationsDetailsRootDto;
import com.roadguard.roadGuard_backend.dto.ResolveLocationDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenStreetReverseLocationService implements ReverseLocationService {

    private final RestClient client;
    private final ModelMapper modelMapper;

    public LocationDetailsDto resolveLocation(ResolveLocationDto request){

        Double longitude = request.getLongitude();
        Double latitude = request.getLatitude();

        LocationsDetailsRootDto locationsDetailsRootDto;

        try {
            Map<String, Object> response = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/reverse")
                            .queryParam("format", "jsonv2")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .build()
                    )
                    .retrieve().body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });


            if (response == null || !response.containsKey("address")) {
                throw new RuntimeException("Geocoding data is missing or incomplete in the response.");
            }
            Map<String, Object> address = (Map<String, Object>) response.get("address");

            locationsDetailsRootDto = modelMapper.map(address, LocationsDetailsRootDto.class);

            String finalAddress = ((String) response.get("display_name"));



            // city -> check town first (since Nominatim returned "town" in example), fallback to request info
            String village = locationsDetailsRootDto.getVillage();

            // block -> check county (e.g., "Koraput Town"), fallback to road (e.g., "NH26"), then request info
            String block = locationsDetailsRootDto.getCounty();

            LocationDetailsDto locationDetailsDto = new LocationDetailsDto();
            modelMapper.map(locationsDetailsRootDto, locationDetailsDto);
            locationDetailsDto.setWard(village);  // Custom logic
            locationDetailsDto.setBlock(block);
            locationDetailsDto.setFullAddress(finalAddress);// Custom logic
            return locationDetailsDto;

        }
        catch (Exception e){
            throw new RuntimeException("Error in getting location data:" + e);
        }
    }


}
