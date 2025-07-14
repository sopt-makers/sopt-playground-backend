package org.sopt.makers.internal.common.auth.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    HttpStatus getStatus();

    String getMessage();
}