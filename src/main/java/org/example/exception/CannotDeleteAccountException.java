package org.example.exception;

public class CannotDeleteAccountException extends RuntimeException {
    public CannotDeleteAccountException(String message) {
        super(message);
    }
}
