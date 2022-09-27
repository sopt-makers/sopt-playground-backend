package org.sopt.makers.internal.exception;

public class ClientBadRequestException extends BusinessLogicException {
    public ClientBadRequestException(String message) { super("[ClientBadRequestException] : " + message); }
}
