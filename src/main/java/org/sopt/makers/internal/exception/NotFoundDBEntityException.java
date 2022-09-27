package org.sopt.makers.internal.exception;

public class NotFoundDBEntityException extends BusinessLogicException {
    public NotFoundDBEntityException(String entityType) {
        super("[NotFoundDBEntityException] Entity type : " + entityType);
    }
}
