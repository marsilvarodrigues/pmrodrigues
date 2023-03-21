package com.pmrodrigues.users.service;

import com.pmrodrigues.security.roles.Security;
import com.pmrodrigues.users.clients.RoleClient;
import com.pmrodrigues.users.model.User;
import com.pmrodrigues.users.repositories.KeycloakUserRepository;
import com.pmrodrigues.users.repositories.UserRepository;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.security.RolesAllowed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.pmrodrigues.users.specifications.SpecificationUser.externalId;
import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
@Service
@RequiredArgsConstructor
@RolesAllowed({Security.SYSTEM_ADMIN})
public class RoleService {

    private final KeycloakUserRepository keycloakUserRepository;

    private final RoleClient roleClient;

    private final UserRepository userRepository;


    @Timed(histogram = true, value = "RoleService.applyRoleToUser")
    public void applyRoleToUser(@NonNull User user, @NonNull String roleName) {
        log.info("apply to user {} a role {}", user, roleName);
        keycloakUserRepository.applyRoleInUser(user, roleName);
    }

    @Timed(histogram = true, value="RoleService.getUserInRole")
    public Page<User> getUserInRole(@NonNull String roleName, @NonNull PageRequest pageRequest) {
        log.info("List all user in {}", roleName);
        val response = roleClient.getUsersInRole(roleName);

        val externalsId =  Optional.ofNullable(response.getBody())
                .orElse(List.of())
                .stream()
                .map(UserRepresentation::getId)
                .map(UUID::fromString)
                .collect(Collectors.toList());

        return userRepository.findAll(where(externalId(externalsId)),pageRequest);

    }
}
