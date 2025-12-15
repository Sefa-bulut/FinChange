package com.example.finchange.customer.exception;

import com.example.finchange.common.exception.AbstractInvalidTransactionException;

public class AccountHasBalanceException extends AbstractInvalidTransactionException {
    public AccountHasBalanceException(String message) {
        super(message);
    }
}
