package org.sopt.makers.internal.exception;

public class FeignClientException extends BusinessLogicException {
    public FeignClientException (String message) { super("[FeignClientException] : " + message); }
}
