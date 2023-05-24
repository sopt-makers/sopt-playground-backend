package org.sopt.makers.internal.exception;

public class WrongImageInputException extends BusinessLogicException {
    public String code;

    public WrongImageInputException (String message, String code) {
        super(message);
        this.code = code;
    }

}
