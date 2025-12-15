package com.example.finchange.common.exception;

public class FileReadException extends RuntimeException {
  public FileReadException(Throwable cause) {
    super("Error occurred while reading file", cause);
  }
}
