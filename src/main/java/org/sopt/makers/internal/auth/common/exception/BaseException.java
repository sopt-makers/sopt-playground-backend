package org.sopt.makers.internal.auth.common.exception;

import org.sopt.makers.internal.auth.common.code.FailureCode;

public abstract class BaseException extends RuntimeException {

    private final FailureCode failure;

    public BaseException(final FailureCode failure) {
        super(failure.getMessage());
        this.failure = failure;
    }

    public FailureCode getError() {
        return this.failure;
    }
}