package org.sopt.makers.internal.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super("[ConflictException]: " + message);
    }
}
