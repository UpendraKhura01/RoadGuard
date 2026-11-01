package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.RoadInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoadInformationRepository extends JpaRepository<RoadInformation, Long> {
    RoadInformation findByLatitudeAndLongitude(Double latitude, Double longitude);

    @Query("SELECT r FROM RoadInformation r JOIN FETCH r.contractor WHERE r.contractor.id = :contractorId")
    List<RoadInformation> findByContractorIdWithContractor(@Param("contractorId") Long contractorId);

    @Query("SELECT r FROM RoadInformation r WHERE " +
            "r.latitude BETWEEN :minLat AND :maxLat AND " +
            "r.longitude BETWEEN :minLon AND :maxLon")
    List<RoadInformation> findInBoundingBox(@Param("minLat") Double minLat,
                                            @Param("maxLat") Double maxLat,
                                            @Param("minLon") Double minLon,
                                            @Param("maxLon") Double maxLon);

    default List<RoadInformation> findNearby(Double lat, Double lon, Double radius) {
        Double minLat = lat - radius;
        Double maxLat = lat + radius;
        Double minLon = lon - radius;
        Double maxLon = lon + radius;

        return findInBoundingBox(minLat, maxLat, minLon, maxLon).stream()
                .filter(r -> Math.sqrt(Math.pow(r.getLatitude() - lat, 2) + Math.pow(r.getLongitude() - lon, 2)) < radius)
                .toList();
    }
}