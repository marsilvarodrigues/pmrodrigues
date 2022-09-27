package com.pmrodrigues.commons.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason = "Failed to connect to Keycloak")
public class KeycloakIntegrationFailed extends RuntimeException {
}
