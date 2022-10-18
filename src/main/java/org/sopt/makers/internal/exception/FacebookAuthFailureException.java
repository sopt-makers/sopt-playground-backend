package org.sopt.makers.internal.exception;

public class FacebookAuthFailureException extends BusinessLogicException {
    public FacebookAuthFailureException (String message) { super("[FacebookAuthFailureException] : " + message); }
}
