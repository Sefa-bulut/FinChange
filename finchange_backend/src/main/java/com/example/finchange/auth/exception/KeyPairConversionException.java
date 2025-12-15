package com.example.finchange.auth.exception;

public class KeyPairConversionException extends RuntimeException {
    public KeyPairConversionException(Throwable cause) {
        super("error occurred while converting key pair", cause);
    }
}
