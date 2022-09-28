package com.pmrodrigues.commons.controlleradvices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ValidationErrorControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            log.error("failed to validate {} {} {}",error.getObjectName(), fieldName, errorMessage);
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Map<String, String> handleConstraintException(ConstraintViolationException ex, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        HttpServletRequest httpRequest = (HttpServletRequest) ((ServletWebRequest)request).getNativeRequest();
        ex.getConstraintViolations().forEach(error -> {
            String methodName = error.getPropertyPath().toString().split("\\.")[1];
            String argsName = error.getPropertyPath().toString().split("\\.")[2];
            String url = httpRequest.getRequestURL().toString();
            String queryString = httpRequest.getQueryString();
            String errorMessage = error.getMessage();
            String beanNode = error.getLeafBean().getClass().getName();
            log.error("failed to validate {}?{} {} {} {} {}", url , queryString , beanNode , methodName, argsName, errorMessage);
            errors.put(String.format("%s", queryString), errorMessage);
        });
        return errors;
    }
}
