package com.example.finchange.customer.exception;

import com.example.finchange.common.exception.AbstractInvalidTransactionException;

public class InsufficientAvailableBalanceException extends AbstractInvalidTransactionException {
    public InsufficientAvailableBalanceException(String message) {
        super(message);
    }
}
