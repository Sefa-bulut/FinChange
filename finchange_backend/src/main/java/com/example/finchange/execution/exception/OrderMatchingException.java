package com.example.finchange.execution.exception;

public class OrderMatchingException extends RuntimeException {
    public OrderMatchingException(String message) {
        super(message);
    }
}