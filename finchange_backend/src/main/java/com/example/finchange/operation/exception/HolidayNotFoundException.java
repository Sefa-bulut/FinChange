package com.example.finchange.operation.exception;

import com.example.finchange.common.exception.AbstractNotFoundException;

public class HolidayNotFoundException extends AbstractNotFoundException {
    public HolidayNotFoundException(String message) {
        super(message);
    }
}
