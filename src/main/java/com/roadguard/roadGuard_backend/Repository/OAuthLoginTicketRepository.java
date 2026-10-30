package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.OAuthLoginTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OAuthLoginTicketRepository extends JpaRepository<OAuthLoginTicket, Long> {
    void deleteByExpiresAtBefore(LocalDateTime now);

    Optional<OAuthLoginTicket> findByCode(String code);
}