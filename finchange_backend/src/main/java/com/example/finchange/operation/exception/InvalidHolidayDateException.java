package com.example.finchange.operation.exception;

import com.example.finchange.common.exception.AbstractInvalidTransactionException;

public class InvalidHolidayDateException extends AbstractInvalidTransactionException {
    public InvalidHolidayDateException(String message) {
        super(message);
    }
}
