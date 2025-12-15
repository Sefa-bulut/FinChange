package com.example.finchange.customer.exception;

import com.example.finchange.common.exception.AbstractNotFoundException;

public class AccountNotFoundException extends AbstractNotFoundException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
