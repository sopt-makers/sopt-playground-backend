package org.sopt.makers.internal.exception;

public class ForbiddenClientException extends BusinessLogicException {
    public ForbiddenClientException (String message) { super("[ForbiddenClientException] : " + message); }
}
