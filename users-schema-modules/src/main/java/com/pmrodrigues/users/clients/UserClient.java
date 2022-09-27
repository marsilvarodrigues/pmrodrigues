package com.pmrodrigues.users.clients;

import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "user", url = "${client.user.location}/auth/admin/realms/master/users")
@Timed(histogram = true, value = "keycloack-integration")
public interface UserClient {

    @RequestMapping(method = RequestMethod.POST,consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity add(@RequestBody @Valid UserRepresentation user);

    @RequestMapping(method = RequestMethod.GET,consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserRepresentation>> getByEmail(@RequestParam("email") @NonNull String email);

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity delete(@PathVariable("id") @NonNull UUID userId);


    @RequestMapping(method = RequestMethod.PUT, path="/{id}",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity update(@PathVariable("id") @NonNull UUID userId, @RequestBody @Valid UserRepresentation user);
}
