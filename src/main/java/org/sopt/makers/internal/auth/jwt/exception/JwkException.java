package org.sopt.makers.internal.auth.jwt.exception;

import org.sopt.makers.internal.auth.common.exception.BaseException;
import org.sopt.makers.internal.auth.jwt.code.JwkFailure;

public class JwkException extends BaseException {
    public JwkException(JwkFailure failure) {
        super(failure);
    }
}