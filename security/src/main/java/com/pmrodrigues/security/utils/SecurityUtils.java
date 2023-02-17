package com.pmrodrigues.security.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.keycloak.KeycloakPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtils {

    public static Optional<UUID> getUserLoggedId() {
        return Optional.ofNullable((KeycloakPrincipal) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal())
                .stream()
                .map(KeycloakPrincipal::getName)
                .map(UUID::fromString)
                .findFirst();
    }

    public static boolean isUserInRole(String... roles) {
        val expectedRoles = asList(roles);
        val auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        return auth.getAuthorities()
                .stream()
                .anyMatch(role -> expectedRoles.contains(role));
    }

}
