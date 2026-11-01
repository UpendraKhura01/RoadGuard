package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Long> {
    Contractor findByPhoneNumber(String phoneNumber);
}