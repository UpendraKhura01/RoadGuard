package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.Otp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {


    @Transactional
    void deleteByPhoneNumber(String phoneNumber);

    Optional<Otp> findByPhoneNumber(String phoneNumber);
}