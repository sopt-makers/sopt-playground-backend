package org.sopt.makers.internal.exception;

public class MemberHasNotProfileException extends BusinessLogicException {

    public MemberHasNotProfileException(String message) {
        super("[MemberHasNotProfileException] : " + message);
    }
}
