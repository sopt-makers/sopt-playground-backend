package org.sopt.makers.internal.exception;

public class WrongTokenException extends BusinessLogicException {
    public WrongTokenException (String message) { super("[WrongTokenException] : " + message); }
}
