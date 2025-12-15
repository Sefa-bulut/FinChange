package com.example.finchange.auth.exception;

import com.example.finchange.common.exception.AbstractAuthException;

import java.io.Serial;

public final class PasswordMismatchException extends AbstractAuthException {

    @Serial
    private static final long serialVersionUID = -8029348572398475621L;

    public PasswordMismatchException(String message) {
        super("Şifreler uyuşmuyor.");
    }
}