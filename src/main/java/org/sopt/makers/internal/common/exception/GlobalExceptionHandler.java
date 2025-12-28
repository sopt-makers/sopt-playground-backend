package org.sopt.makers.internal.common.exception;

import feign.FeignException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.exception.*;
import org.sopt.makers.internal.external.slack.MessageType;
import org.sopt.makers.internal.external.slack.SlackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final SlackService slackService;

    @ExceptionHandler(PlaygroundException.class)
    public ResponseEntity<String> businessLogicException (PlaygroundException ex, final HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentNotValidException (MethodArgumentNotValidException ex) {
        log.error(ex.getMessage());

        Errors errors = ex.getBindingResult();
        Map<String, String> validateDetails = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validateDetails.put(validKeyName, error.getDefaultMessage());
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(validateDetails);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> entityNotfoundException (EntityNotFoundException ex, final HttpServletRequest request) {

        sendErrorMessageToSlack(ex, MessageType.CLIENT, request);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    // TODO 공통 Error Response 생성 후 일괄 적용
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadableException (HttpMessageNotReadableException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> clientBadRequestException (BadRequestException ex, final HttpServletRequest request) {

        sendErrorMessageToSlack(ex, MessageType.CLIENT, request);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> forbiddenClientException (ForbiddenException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> UnauthorizedException (UnauthorizedException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> feignClientException(FeignException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid external api request" + ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> unknownException (RuntimeException ex, final HttpServletRequest request) {
        sendErrorMessageToSlack(ex, MessageType.SERVER, request);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKey(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    private void sendErrorMessageToSlack(Exception exception, MessageType messageType, final HttpServletRequest request) {
        LinkedHashMap<String, String> content = new LinkedHashMap<>();

        content.put("🌍 Environment", activeProfile);
        content.put("📍 Error Location", getErrorLocation(exception));
        content.put("⚠️ Exception Type", exception.getClass().getSimpleName());
        content.put("💬 Error Message", exception.getMessage());
        slackService.sendMessage(messageType.getTitle(), content, messageType, request);
    }

    private String getErrorLocation(Throwable throwable) {
        if (throwable.getStackTrace().length == 0) {
            return "Unknown location";
        }

        StackTraceElement firstTrace = throwable.getStackTrace()[0];
        return String.format("%s.%s() (Line %d)",
                firstTrace.getClassName(),
                firstTrace.getMethodName(),
                firstTrace.getLineNumber()
        );
    }
}
