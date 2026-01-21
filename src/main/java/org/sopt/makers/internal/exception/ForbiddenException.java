package org.sopt.makers.internal.exception;

public class ForbiddenException extends PlaygroundException {
    public ForbiddenException(String message) {
        super("ForbiddenException: " + message);
    }
}
