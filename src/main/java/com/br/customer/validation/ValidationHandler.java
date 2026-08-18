package com.br.customer.validation;

import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.exceptions.DuplicateCpfException;
import com.br.score.exceptions.ScoreNotFoundException;
import com.br.score.exceptions.ScoreServiceTimeoutException;
import com.br.score.exceptions.ScoreServiceUnavailableException;
import com.br.score.exceptions.ScoreUnexpectedResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ValidationHandler {

    public record ErrorResponse(String mensagem, Integer codigoStatus) {
    }

    public record ValidationErrorResponse(String field, String message) {
        public ValidationErrorResponse(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<ValidationErrorResponse> errors = exception.getFieldErrors().stream()
                .map(ValidationErrorResponse::new)
                .toList();
        String errorDetails = errors.stream()
                .map(error -> String.format("%s: %s", error.field(), error.message()))
                .collect(Collectors.joining(", "));
        log.warn("Request validation failed. Details: {}", errorDetails);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException exception) {
        log.warn("Business logic error: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(DuplicateCpfException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCpf(DuplicateCpfException exception) {
        log.warn("Business logic error: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = String.format("Invalid value '%s' for parameter '%s'", exception.getValue(), exception.getName());
        log.warn("Method argument type mismatch. Details: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ScoreServiceTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleScoreTimeout(ScoreServiceTimeoutException exception) {
        log.error("External service communication error: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.GATEWAY_TIMEOUT.value()));
    }

    @ExceptionHandler(ScoreServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleScoreUnavailable(ScoreServiceUnavailableException exception) {
        log.error("External service communication error: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.SERVICE_UNAVAILABLE.value()));
    }

    @ExceptionHandler(ScoreUnexpectedResponseException.class)
    public ResponseEntity<ErrorResponse> handleScoreUnexpected(ScoreUnexpectedResponseException exception) {
        log.error("External service communication error: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.BAD_GATEWAY.value()));
    }

    @ExceptionHandler(ScoreNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScoreNotFound(ScoreNotFoundException exception) {
        log.warn("External service business logic error: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        String message = exception.getMessage() == null ? "null" : exception.getMessage();
        log.error("An unexpected error occurred.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error: " + message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}