package com.example.finchange.user.exception;

import com.example.finchange.common.exception.AbstractAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class UserAlreadyExistsException extends AbstractAlreadyExistsException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}