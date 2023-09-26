package com.pmrodrigues.users.exceptions;

import com.pmrodrigues.commons.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Phone(s) not found")
public class PhoneNotFoundException extends NotFoundException {
}
