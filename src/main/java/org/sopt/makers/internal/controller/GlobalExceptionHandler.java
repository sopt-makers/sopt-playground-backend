package org.sopt.makers.internal.controller;

import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<String> businessLogicException (BusinessLogicException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentNotValidException (MethodArgumentNotValidException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> entityNotfoundException (EntityNotFoundException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ClientBadRequestException.class)
    public ResponseEntity<String> entityNotfoundException (ClientBadRequestException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> entityNotfoundException (RuntimeException ex) {
        log.error("[Unknown Error] : " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

}
