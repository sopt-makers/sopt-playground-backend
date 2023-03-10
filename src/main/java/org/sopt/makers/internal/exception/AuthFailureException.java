package org.sopt.makers.internal.exception;

public class AuthFailureException extends BusinessLogicException {
    public AuthFailureException(String message) { super("[AuthFailureException] : " + message); }
}
