package com.example.finchange.execution.exception;

public class SettlementFailedException extends RuntimeException {

    public SettlementFailedException(String message) {
        super(message);
    }

    public SettlementFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}