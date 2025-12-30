package org.sopt.makers.internal.exception;

public class ConflictException extends PlaygroundException {
    public ConflictException(String message) {
        super("ConflictException: " + message);
    }
}
