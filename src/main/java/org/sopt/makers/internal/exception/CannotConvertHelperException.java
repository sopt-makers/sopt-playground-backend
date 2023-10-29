package org.sopt.makers.internal.exception;

public class CannotConvertHelperException extends BusinessLogicException{
    public CannotConvertHelperException(String message) {
        super("[CannotConvertHelperException] : " + message);
    }
}
