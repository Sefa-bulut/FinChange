package com.example.finchange.operation.exception;

import com.example.finchange.common.exception.AbstractAlreadyExistsException;

import java.time.LocalDate;

public class HolidayAlreadyException extends AbstractAlreadyExistsException {
    public HolidayAlreadyException(LocalDate date) {
        super("Bu tarih zaten tatil olarak eklenmiş: " + date);
    }
}

