package com.example.pismo_challenge.Exception;

import com.example.pismo_challenge.DTO.Error.ErrorResponseMessage;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseMessage> handleEntityNotFound(EntityNotFoundException ex) {
        var error = ErrorResponseMessage.builder()
                .errorMessage(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponseMessage> handleEntityFound(EntityExistsException ex) {
        var error = ErrorResponseMessage.builder()
                .errorMessage(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseMessage> handleIllegalArgument(IllegalArgumentException ex) {
        var error = ErrorResponseMessage.builder()
                .errorMessage(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseMessage> handleArgumentNotValid(MethodArgumentNotValidException ex) {
        var error = ErrorResponseMessage.builder()
                .errorMessage("Preencha os dados corretamente")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseMessage> handleIllegalState(IllegalStateException ex) {
        var error = ErrorResponseMessage.builder()
                .errorMessage(ex.getMessage())
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
}