package com.example.finchange.customer.exception;

import com.example.finchange.common.exception.AbstractAlreadyExistsException;

public class AccountNameAlreadyExistsException extends AbstractAlreadyExistsException {
    public AccountNameAlreadyExistsException(String message) {
        super(message);
    }
}
