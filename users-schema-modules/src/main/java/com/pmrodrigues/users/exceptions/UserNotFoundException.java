package com.pmrodrigues.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="User(s) not found")
public class UserNotFoundException extends RuntimeException {}
