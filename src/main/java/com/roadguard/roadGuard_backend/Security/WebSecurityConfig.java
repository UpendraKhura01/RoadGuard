package com.roadguard.roadGuard_backend.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        httpSecurity.csrf(csrfConfigurer -> csrfConfigurer.disable())
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "AUTHORITY")
                                .requestMatchers("/user/**").authenticated()
                                .requestMatchers("/notification/**").authenticated()
                                .requestMatchers("/AiAssessment/**").hasAnyRole("ADMIN", "AUTHORITY")
                                .requestMatchers("/citizen/**").authenticated()
                                .requestMatchers("/feedback/**").authenticated()

                                .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2.failureHandler(
                        (request, response, exception) -> {
                            throw new RuntimeException();
                        }
                ).successHandler(oAuth2SuccessHandler))

                .httpBasic(httpBasicConfigurer -> httpBasicConfigurer.disable());
        return httpSecurity.build();
    }
}
