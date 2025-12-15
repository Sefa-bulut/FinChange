package com.example.finchange.common.exception;

import java.io.Serial;

public abstract class AbstractInvalidTransactionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AbstractInvalidTransactionException(String message) {
        super(message);
    }

    protected AbstractInvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
