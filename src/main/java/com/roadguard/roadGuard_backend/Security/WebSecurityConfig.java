package com.roadguard.roadGuard_backend.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        httpSecurity.csrf(csrfConfigurer -> csrfConfigurer.disable())
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/").permitAll()
                                .anyRequest().permitAll()
                )
                .httpBasic(httpBasicConfigurer -> httpBasicConfigurer.disable());
        return httpSecurity.build();
    }
}
