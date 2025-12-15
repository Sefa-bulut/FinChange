package com.example.finchange.customer.exception;

import com.example.finchange.common.exception.AbstractInvalidTransactionException;

public class InvalidTransactionAmountException extends AbstractInvalidTransactionException {

    public InvalidTransactionAmountException(String message) {
        super(message);
    }

    public InvalidTransactionAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}

