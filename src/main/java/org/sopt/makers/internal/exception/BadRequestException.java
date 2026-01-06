package org.sopt.makers.internal.exception;

public class BadRequestException extends PlaygroundException {
    public BadRequestException(String message) {
        super("BadRequestException: " + message);
    }
}
