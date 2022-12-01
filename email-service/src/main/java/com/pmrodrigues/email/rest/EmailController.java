package com.pmrodrigues.email.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pmrodrigues.commons.dtos.Email;
import com.pmrodrigues.email.service.EmailService;
import com.pmrodrigues.email.service.EmailTemplateService;
import com.pmrodrigues.security.roles.Security;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.annotation.security.RolesAllowed;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService service;
    @Autowired
    private EmailTemplateService templateService;


    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity exceptionHandler(JsonProcessingException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @Timed(histogram = true)
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Send a email", nickname = "send", tags={ "email", }, httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Push a email to be sent"),
            @ApiResponse(code = 400, message = "An error to send a email")})
    @RolesAllowed({Security.SYSTEM_ADMIN})
    public ResponseEntity send(@NonNull @RequestBody final Email email) throws JsonProcessingException {
        log.debug("send email to {}", email);
        service.send(email);
        return ResponseEntity.ok().build();
    }

    @Timed(histogram = true)
    @GetMapping(consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a template by his name", nickname = "getTemplate", tags={ "email", }
            , consumes = APPLICATION_JSON_VALUE, httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Push a email to be sent", response = Email.class),
            @ApiResponse(code = 404, message = "Template not found"),
            @ApiResponse(code = 400, message = "An error to get a template")})
    @RequestMapping(value = "/{template}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @RolesAllowed({Security.SYSTEM_ADMIN})
    public ResponseEntity<Email> getEmailByName(@NonNull @PathVariable("template") final String template) {
        log.info("try to get the template {}", template);
        val email = templateService.getByEmailType(template);

        if( log.isDebugEnabled() ) {
            log.debug("template {} found {}", template, email);
        }

        return ResponseEntity.ok(email);
    }
}
