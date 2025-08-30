package org.sopt.makers.internal.auth.jwt.exception;

import org.sopt.makers.internal.auth.common.exception.BaseException;
import org.sopt.makers.internal.auth.jwt.code.JwtFailure;

public class JwtException extends BaseException {
    public JwtException(JwtFailure failure) {
        super(failure);
    }
}