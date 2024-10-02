package org.sopt.makers.internal.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.dto.CommonExceptionResponse;
import org.sopt.makers.internal.dto.auth.RegisterTokenBySmsResponse;
import org.sopt.makers.internal.dto.sopticle.SopticleResponse;
import org.sopt.makers.internal.dto.soulmate.SoulmateResponse;
import org.sopt.makers.internal.exception.*;
import org.sopt.makers.internal.external.slack.MessageType;
import org.sopt.makers.internal.external.slack.SlackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SlackService slackService;

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<String> businessLogicException (BusinessLogicException ex) {

//        sendErrorMessageToSlack(ex, MessageType.CLIENT);
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

    @ExceptionHandler(ClientBadRequestException.class)
    public ResponseEntity<String> clientBadRequestException (ClientBadRequestException ex, final HttpServletRequest request) {

        sendErrorMessageToSlack(ex, MessageType.CLIENT, request);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(WrongImageInputException.class)
    public ResponseEntity<CommonExceptionResponse> wrongImageInputException (WrongImageInputException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CommonExceptionResponse(ex.getMessage(), ex.code));
    }

    @ExceptionHandler(ForbiddenClientException.class)
    public ResponseEntity<String> forbiddenClientException (ForbiddenClientException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AuthFailureException.class)
    public ResponseEntity<String> authFailureException (AuthFailureException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    @ExceptionHandler(WrongAccessTokenException.class)
    public ResponseEntity<String> wrongAccessTokenException (WrongAccessTokenException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(WrongSecretHeaderException.class)
    public ResponseEntity<String> wrongSecretHeaderException (WrongSecretHeaderException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(WrongSixNumberCodeException.class)
    public ResponseEntity<RegisterTokenBySmsResponse> wrongSixNumberCodeException (WrongSixNumberCodeException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new RegisterTokenBySmsResponse(false, ex.getMessage(), null, null));
    }

    @ExceptionHandler(SopticleException.class)
    public ResponseEntity<SopticleResponse> duplicateSopticleWriterException (SopticleException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new SopticleResponse(false, ex.getMessage(), null));
    }

    @ExceptionHandler(SoulmateException.class)
    public ResponseEntity<SoulmateResponse> SoulmateException (SoulmateException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new SoulmateResponse(false, ex.getMessage(), null));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> feignClientException(FeignException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid external api request");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> unknownException (RuntimeException ex, final HttpServletRequest request) {
        sendErrorMessageToSlack(ex, MessageType.SERVER, request);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    private void sendErrorMessageToSlack(Exception exception, MessageType messageType, final HttpServletRequest request) {
        LinkedHashMap<String, String> content = new LinkedHashMap<>();

        content.put("Exception Class", exception.getClass().getName());
        content.put("Exception Message", exception.getMessage());
        content.put("Stack Trace", getStackTraceAsString(exception));
        slackService.sendMessage(messageType.getTitle(), content, messageType, request);
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
