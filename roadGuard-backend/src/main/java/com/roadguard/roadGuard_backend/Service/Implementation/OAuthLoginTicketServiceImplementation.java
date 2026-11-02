package com.roadguard.roadGuard_backend.Service.Implementation;

import com.roadguard.roadGuard_backend.Repository.OAuthLoginTicketRepository;
import com.roadguard.roadGuard_backend.Repository.UserRepository;
import com.roadguard.roadGuard_backend.Service.OAuthLoginTicketService;
import com.roadguard.roadGuard_backend.entity.OAuthLoginTicket;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class OAuthLoginTicketServiceImplementation implements OAuthLoginTicketService {

    private final OAuthLoginTicketRepository oAuthLoginTicketRepository;
    private final UserRepository userRepository;

    private static final int EXPIRATION_MINUTES = 5;

    @Override
    @Transactional
    public String createTicket(Long userId) {
        oAuthLoginTicketRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        String code = UUID.randomUUID().toString().replace("-", "");
        OAuthLoginTicket ticket = OAuthLoginTicket.builder()
                .code(code)
                .userId(userId)
                .used(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();

        oAuthLoginTicketRepository.save(ticket);
        return code;
    }

    @Override
    @Transactional
    public User consumeTicket(String code) {
        OAuthLoginTicket ticket = oAuthLoginTicketRepository.findByCode(code)
                .orElse(null);

        if (ticket == null) {
            throw new IllegalArgumentException("Invalid or expired code");
        }

        if (ticket.isUsed()) {
            throw new IllegalArgumentException("Code has already been used");
        }

        if (ticket.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Code has expired");
        }

        ticket.setUsed(true);
        oAuthLoginTicketRepository.save(ticket);

        return userRepository.findById(ticket.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
