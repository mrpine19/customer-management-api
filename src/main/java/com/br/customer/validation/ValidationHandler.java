package com.br.customer.validation;

import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.exceptions.DuplicateCpfException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Customer not found: " + exception.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(DuplicateCpfException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCpf(DuplicateCpfException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Duplicate CPF: " + exception.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
