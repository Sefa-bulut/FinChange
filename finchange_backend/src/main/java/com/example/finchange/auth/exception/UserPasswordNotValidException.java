package com.example.finchange.auth.exception;

import com.example.finchange.common.exception.AbstractAuthException;

import java.io.Serial;

public final class UserPasswordNotValidException extends AbstractAuthException {

    @Serial
    private static final long serialVersionUID = 359664997679732461L;

    public UserPasswordNotValidException() {
        super("şifre hatalı");
    }

}
