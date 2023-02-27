package org.sopt.makers.internal.exception;

public class WrongAccessTokenException extends BusinessLogicException {

    public WrongAccessTokenException (String message) { super("[WrongAccessTokenException] : " + message); }
}
