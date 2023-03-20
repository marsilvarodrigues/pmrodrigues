package com.pmrodrigues.users.clients;

import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "user", url = "${client.user.location}/auth/admin/realms/master/users")
@Timed(histogram = true, value = "keycloack-integration")
public interface UserClient {

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<String> add(@RequestBody @Valid UserRepresentation user);

    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserRepresentation>> getByEmail(@RequestParam("email") @NonNull String email);

    @DeleteMapping(path = "/{id}",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<String> delete(@PathVariable("id") @NonNull UUID userId);


    @PutMapping(path="/{id}",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<String> update(@PathVariable("id") @NonNull UUID userId, @RequestBody @Valid UserRepresentation user);

    @GetMapping(path="/{id}",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<UserRepresentation> getById(@PathVariable("id") @NonNull UUID userId);
}
