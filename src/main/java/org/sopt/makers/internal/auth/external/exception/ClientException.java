package org.sopt.makers.internal.auth.external.exception;

import org.sopt.makers.internal.auth.common.exception.BaseException;
import org.sopt.makers.internal.auth.external.code.ClientFailure;

public class ClientException extends BaseException {
    public ClientException(ClientFailure failure) {
        super(failure);
    }
}