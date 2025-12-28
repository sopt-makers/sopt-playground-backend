package org.sopt.makers.internal.exception;

public class NotFoundException extends PlaygroundException {
    public NotFoundException(String message) {
        super("NotFoundException: " + message);
    }
}
