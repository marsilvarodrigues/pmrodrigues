package com.pmrodrigues.users.repositories;

import com.pmrodrigues.commons.exceptions.KeycloakIntegrationFailed;
import com.pmrodrigues.users.clients.UserClient;
import com.pmrodrigues.users.keycloak.UserFactory;
import com.pmrodrigues.users.model.User;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserRepository {
    private final UserClient userClient;
    @Timed(histogram = true, value = "KeycloakUserRepository.insert")
    public UUID insert(@NonNull final User user) {
        log.info("saving user {} into Keycloak", user);

        val response = userClient.add(UserFactory.createUser(user));
        if (response.getStatusCode() != HttpStatus.CREATED) {
            log.error("error to save in Keycloack {} - {}", response.getStatusCode(), response.getBody());
            throw new KeycloakIntegrationFailed();
        }

        log.debug("User {} saved {}", user, response.getHeaders().getLocation().getPath());
        return UUID.fromString(response.getHeaders().getLocation().getPath().split("/auth/admin/realms/master/users/")[1]);
    }

    @Timed(histogram = true, value = "KeycloakUserRepository.getUserIdByEmail")
    public Map<String, String> getUserIdByEmail(@NonNull final String email) {
        log.info("searching into keycloak users with this email {}", email);
        var response = userClient.getByEmail(email);
        if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(userClient.getByEmail(email).getBody())
                        .orElse(List.of())
                        .stream()
                        .collect(Collectors.toMap(UserRepresentation::getEmail, UserRepresentation::getId));
        } else {
            return Map.of();
        }
    }

    @Timed(histogram = true, value = "KeycloakUserRepository.delete")
    public void delete(@NonNull final UUID userId){
        log.info("delete user by {} into keycloak", userId);
        val response = userClient.delete(userId);
        if(response.getStatusCode() != HttpStatus.OK){
            log.error("error to delete in user {} in Keycloack {} - {}", userId, response.getStatusCode(), response.getBody());
            throw new KeycloakIntegrationFailed();
        }
    }

    @Timed(histogram = true, value = "KeycloakUserRepository.update")
    public void update(@NonNull final User user) {
        log.info("updating the user {} in keycloack", user);
        val keycloackUser = UserFactory.createUser(user);
        var response = userClient.update(user.getExternalId(),keycloackUser);
        if(response.getStatusCode() != HttpStatus.OK){
            log.error("error to update in user {} in Keycloack {} - {}", user, response.getStatusCode(), response.getBody());
            throw new KeycloakIntegrationFailed();
        }
    }
}
