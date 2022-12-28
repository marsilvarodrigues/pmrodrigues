package com.pmrodrigues.security.audit;

import lombok.extern.java.Log;
import org.keycloak.KeycloakPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Log
@Component
public class SpringSecurityAuditAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.ofNullable((KeycloakPrincipal)authentication.getPrincipal())
                .stream()
                .map(KeycloakPrincipal::getName)
                .map(UUID::fromString)
                .findFirst();


    }
}