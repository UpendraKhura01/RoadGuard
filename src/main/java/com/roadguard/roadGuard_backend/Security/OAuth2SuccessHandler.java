package com.roadguard.roadGuard_backend.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadguard.roadGuard_backend.Service.OAuthLoginTicketService;
import com.roadguard.roadGuard_backend.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final OAuthLoginTicketService oAuthLoginTicketService;

    @Value("${APP.DEEP-LINK}")
    String appDeepLink;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        User user = authService.getUserByEmail(email);
        if(user == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        boolean isMobileApp = request.getSession().getAttribute("mobile-flow") != null;


        if(isMobileApp){
            String ticket = oAuthLoginTicketService.createTicket(user.getId());
            response.sendRedirect(appDeepLink + "?code=" + ticket);
            request.getSession().removeAttribute("mobile-flow");
        }
        else{
            //Browser for Testing(no ticke) direct login json
            ResponseEntity<?> loginResponse = authService.oAuth2Login(token);
            response.setStatus(loginResponse.getStatusCode().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(loginResponse.getBody()));

        }

    }
}
