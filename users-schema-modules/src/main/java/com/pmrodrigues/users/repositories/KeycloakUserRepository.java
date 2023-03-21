package com.pmrodrigues.users.repositories;

import com.pmrodrigues.commons.exceptions.KeycloakIntegrationFailed;
import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.users.clients.RoleClient;
import com.pmrodrigues.users.clients.UserClient;
import com.pmrodrigues.users.exceptions.RoleNotFoundException;
import com.pmrodrigues.users.exceptions.UserNotFoundException;
import com.pmrodrigues.users.keycloak.UserFactory;
import com.pmrodrigues.users.model.User;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.security.RolesAllowed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
@RolesAllowed({Security.SYSTEM_ADMIN})
public class KeycloakUserRepository {

    public static final String REGEX_PATTERN = "(?<=\\/)[^\\/]+$";
    private final UserClient userClient;

    private final RoleClient roleClient;

    private UUID getUUID(String path){

        val pattern = Pattern.compile(REGEX_PATTERN);
        val matcher = pattern.matcher(path);

        if(matcher.find()){
            return UUID.fromString(matcher.group());
        }
        return null;
    }

    @Timed(histogram = true, value = "KeycloakUserRepository.insert")
    public UUID insert(@NonNull final User user) {
        log.info("saving user {} into Keycloak", user);

        val response = userClient.add(UserFactory.createUser(user));
        if (response.getStatusCode() != HttpStatus.CREATED) {
            log.error("error to save in Keycloack {} - {}", response.getStatusCode(), response.getBody());
            throw new KeycloakIntegrationFailed();
        }

        log.debug("User {} saved {}", user, response.getHeaders().getLocation().getPath());
        return this.getUUID(response.getHeaders().getLocation().getPath());
    }

    @Timed(histogram = true, value = "KeycloakUserRepository.getUserIdByEmail")
    public Map<String, String> getUserIdByEmail(@NonNull final String email) {
        log.info("searching into keycloak users with this email {}", email);
        var response = userClient.getByEmail(email);
        if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody())
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
        if(response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.NO_CONTENT){
            log.error("error to delete in user {} in Keycloack {} - {}", userId, response.getStatusCode(), response.getBody());
            throw new KeycloakIntegrationFailed();
        }
    }

    @Timed(histogram = true, value = "KeycloakUserRepository.update")
    public void update(@NonNull final User user) {
        log.info("updating the user {} in keycloack", user);
        val keycloackUser = getUserById(user.getExternalId());

        log.debug("user found {}", user);

        keycloackUser.setFirstName(user.getFirstName());
        keycloackUser.setLastName(user.getLastName());
        keycloackUser.setEmail(user.getEmail());

        val response = userClient.update(user.getExternalId(),keycloackUser);
        if(response.getStatusCode() != HttpStatus.NO_CONTENT && response.getStatusCode() != HttpStatus.OK){
            log.error("error to update in user {} in Keycloack {} - {}", user, response.getStatusCode(), response.getBody());
            throw new KeycloakIntegrationFailed();
        }
    }

    public UserRepresentation getUserById(UUID id) {
        var response = userClient.getById(id);
        if( response.getStatusCode() == HttpStatus.NOT_FOUND )
            throw new UserNotFoundException();

        return response.getBody();
    }

    @Timed(histogram = true, value = "KeycloakUserRepository.applyRoleInUser")
    public void applyRoleInUser(@NonNull User user, @NonNull String roleName) {
        log.info("apply to user {} a role {}", user, roleName);
        val userResponse = userClient.getById(user.getExternalId());
        val roleResponse = roleClient.getRole(roleName);

        if(userResponse.getStatusCode() == HttpStatus.NOT_FOUND){
            throw new UserNotFoundException();
        }

        if(roleResponse.getStatusCode() == HttpStatus.NOT_FOUND){
            throw new RoleNotFoundException();
        }

        val keycloakUser = userResponse.getBody();

        keycloakUser.getRealmRoles().add(roleName);
        userClient.update(user.getExternalId(), keycloakUser);
    }
}
