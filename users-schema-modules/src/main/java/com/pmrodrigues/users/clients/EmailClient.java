package com.pmrodrigues.users.clients;

import com.pmrodrigues.commons.dtos.Email;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;



@FeignClient(name = "email", url = "${client.email.location}/email")
@Timed(histogram = true, value = "email-service")
public interface EmailClient {

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    ResponseEntity<String> send(@NonNull @RequestBody final Email email);


    @GetMapping(path = "/{template}"
            , produces = APPLICATION_JSON_VALUE
            , consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<Email> getEmailByName(@NonNull @PathVariable("template") final String template);
}
