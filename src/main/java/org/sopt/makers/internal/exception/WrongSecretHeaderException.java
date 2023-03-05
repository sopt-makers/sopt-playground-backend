package org.sopt.makers.internal.exception;

public class WrongSecretHeaderException extends BusinessLogicException {
    public WrongSecretHeaderException (String message) { super("[WrongSecretHeaderException] : " + message); }
}
