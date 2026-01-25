package com.msi.exception;

public class DailySignInLimitExceededException extends RuntimeException {
    public DailySignInLimitExceededException(String message) {
        super(message);
    }
}

