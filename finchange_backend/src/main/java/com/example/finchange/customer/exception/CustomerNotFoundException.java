package com.example.finchange.customer.exception;

import com.example.finchange.common.exception.AbstractNotFoundException;

public class CustomerNotFoundException extends AbstractNotFoundException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
