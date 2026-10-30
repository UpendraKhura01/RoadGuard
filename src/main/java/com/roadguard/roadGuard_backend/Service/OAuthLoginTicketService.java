package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.entity.User;

public interface OAuthLoginTicketService {

    String createTicket(Long userId);

    User consumeTicket(String code);
}
