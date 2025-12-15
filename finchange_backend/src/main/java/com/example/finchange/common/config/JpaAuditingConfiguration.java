package com.example.finchange.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration
public class JpaAuditingConfiguration {

    @Bean
    public AuditorAware<Integer> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userIdClaim = jwt.getClaimAsString("userId");
            if (userIdClaim != null) {
                try {
                    return Optional.of(Integer.parseInt(userIdClaim));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        };
    }
}