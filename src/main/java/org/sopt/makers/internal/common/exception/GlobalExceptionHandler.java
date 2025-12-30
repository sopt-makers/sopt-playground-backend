package org.sopt.makers.internal.common.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.common.notification.ErrorNotificationService;
import org.sopt.makers.internal.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final ErrorNotificationService errorNotificationService;
    private static final String BASE_PACKAGE = "org.sopt.makers.internal";

    // Business Exception
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenException(ForbiddenException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(ConflictException exception) {
        return createErrorResponse(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PlaygroundException.class)
    public ResponseEntity<Map<String, String>> handlePlaygroundException(PlaygroundException exception, HttpServletRequest request) {
        log.error("PlaygroundException Message: {}, Location: {}, URI: {}",
                exception.getMessage(),
                getErrorLocation(exception),
                request.getRequestURI()
        );

        if (errorNotificationService.shouldNotify(exception, activeProfile)) {
            errorNotificationService.notifyError(exception, request, activeProfile, BASE_PACKAGE);
        }

        return createErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Validation Exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return createErrorResponse("Validation failed: " + errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException exception) {
        String errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return createErrorResponse("Constraint violation: " + errors, HttpStatus.BAD_REQUEST);
    }

    // HTTP Exception
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return createErrorResponse("Invalid request body", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        return createErrorResponse("HTTP method not supported: " + exception.getMethod(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return createErrorResponse("Missing required parameter: " + exception.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        return createErrorResponse("Unsupported media type: " + exception.getContentType(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // Unexpected Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception exception, HttpServletRequest request) {
        log.error("UnexpectedError Message: {}, Location: {}, URI: {}, Method: {}",
                exception.getMessage(),
                getErrorLocation(exception),
                request.getRequestURI(),
                request.getMethod()
        );

        if (errorNotificationService.shouldNotify(exception, activeProfile)) {
            errorNotificationService.notifyError(exception, request, activeProfile, BASE_PACKAGE);
        }

        return createErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    private String getErrorLocation(Throwable throwable) {
        if (throwable.getStackTrace().length == 0) {
            return "Unknown location";
        }

        for (StackTraceElement trace : throwable.getStackTrace()) {
            if (trace.getClassName().startsWith(BASE_PACKAGE)) {
                return String.format("%s.%s() (Line %d)",
                        trace.getClassName(),
                        trace.getMethodName(),
                        trace.getLineNumber()
                );
            }
        }

        StackTraceElement firstTrace = throwable.getStackTrace()[0];
        return String.format("%s.%s() (Line %d)",
                firstTrace.getClassName(),
                firstTrace.getMethodName(),
                firstTrace.getLineNumber()
        );
    }
}
