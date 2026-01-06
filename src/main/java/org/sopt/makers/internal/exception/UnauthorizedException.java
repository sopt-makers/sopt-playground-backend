package org.sopt.makers.internal.exception;

public class UnauthorizedException extends PlaygroundException {
    public UnauthorizedException(String message) {
        super("UnauthorizedException: " + message);
    }
}
