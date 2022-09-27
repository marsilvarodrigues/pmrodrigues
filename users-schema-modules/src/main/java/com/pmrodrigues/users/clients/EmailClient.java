package com.pmrodrigues.users.clients;

import com.pmrodrigues.commons.dtos.Email;
import feign.Headers;
import io.micrometer.core.annotation.Timed;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;



@FeignClient(name = "email", url = "${client.email.location}/email")
@Timed(histogram = true, value = "email-service")
public interface EmailClient {

    @RequestMapping(method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    ResponseEntity send(@NonNull @RequestBody final Email email);


    @RequestMapping(method = RequestMethod.GET ,path = "/{template}"
            , produces = APPLICATION_JSON_VALUE
            , consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<Email> getEmailByName(@NonNull @PathVariable("template") final String template);
}
