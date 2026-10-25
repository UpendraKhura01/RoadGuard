package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.RoadInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadInformationRepository extends JpaRepository<RoadInformation, Long> {
    RoadInformation findByLatitudeAndLongitude(Double latitude, Double longitude);

    List<RoadInformation> findByContractorId(Long contractorId);

    @Query("SELECT r FROM RoadInformation r WHERE " +
            "SQRT(POWER(r.latitude - :lat, 2) + POWER(r.longitude - :lon, 2)) < :radius")
    List<RoadInformation> findNearby(@Param("lat") Double lat,
                                     @Param("lon") Double lon,
                                     @Param("radius") Double radius);
}