package com.msi.exception;

public class SqlInjectionException extends RuntimeException {
    public SqlInjectionException(String message) {
        super(message);
    }
}