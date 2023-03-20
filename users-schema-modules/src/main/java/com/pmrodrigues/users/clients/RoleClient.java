package com.pmrodrigues.users.clients;

import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "role", url = "${client.user.location}/auth/admin/realms/master/roles")
@Timed(histogram = true, value = "keycloack-integration")
public interface RoleClient {

    @GetMapping(path = "/{roleName}",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<RoleRepresentation> getRole(@PathVariable("roleName") @NonNull String roleName);

    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<List<RoleRepresentation>> getRoles();

    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<List<RoleRepresentation>> getRoles(@RequestParam("search") @NonNull String roleName);

    @GetMapping(path = "/{roleName}/users",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserRepresentation>> getUsersInRole(@PathVariable("roleName") @NonNull String roleName);

}
