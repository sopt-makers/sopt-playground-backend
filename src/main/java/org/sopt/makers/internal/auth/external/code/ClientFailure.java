package org.sopt.makers.internal.auth.external.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.auth.common.code.FailureCode;
import org.springframework.http.HttpStatus;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum ClientFailure implements FailureCode {
    RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 서버 응답 오류"),
    COMMUNICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"외부 서버 통신 실패");

    private final HttpStatus status;
    private final String message;
}