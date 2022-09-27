package com.pmrodrigues.commons.controlleradvices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.dao.DuplicateKeyException;


@ControllerAdvice
@Slf4j
public class DuplicatedKeyControllerAdvice {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({DuplicateKeyException.class})
    @ResponseBody
    public String duplicatedKeyException(Exception e){
        log.error("error to save a object {}", e.getMessage(), e);
        return e.getMessage();
    }
}
