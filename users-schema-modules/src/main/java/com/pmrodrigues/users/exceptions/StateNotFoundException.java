package com.pmrodrigues.users.exceptions;

import com.pmrodrigues.commons.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="State not found")
public class StateNotFoundException extends NotFoundException {
}
