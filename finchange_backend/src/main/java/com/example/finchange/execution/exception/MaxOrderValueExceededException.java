package com.example.finchange.execution.exception;

public class MaxOrderValueExceededException extends RuntimeException {
    public MaxOrderValueExceededException(String message) {
        super(message);
    }
}
