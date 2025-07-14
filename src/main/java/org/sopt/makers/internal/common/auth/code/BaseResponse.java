package org.sopt.makers.internal.common.auth.code;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder(access = PRIVATE)
@RequiredArgsConstructor(access = PRIVATE)
public class BaseResponse<T> {

    @JsonProperty("success")
    private final boolean isSuccess;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("data")
    private final T data;

    public static <T> BaseResponse<?> ofFailure(FailureCode failure, T data) {
        return new BaseResponse<>(false, failure.getMessage(), data);
    }

    public static BaseResponse<?> ofFailure(FailureCode failure) {
        return new BaseResponse<>(false, failure.getMessage(), null);
    }

    public static <T> BaseResponse<?> ofSuccess(SuccessCode success, T data) {
        return new BaseResponse<>(true, success.getMessage(), data);
    }

    public static BaseResponse<?> ofSuccess(SuccessCode success) {
        return new BaseResponse<>(true, success.getMessage(), null);
    }
}
