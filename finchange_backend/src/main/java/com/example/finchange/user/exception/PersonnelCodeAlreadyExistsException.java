package com.example.finchange.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // HTTP 409
public class PersonnelCodeAlreadyExistsException extends UserAlreadyExistsException {
    public PersonnelCodeAlreadyExistsException(String message) {
        super(message);
    }
}