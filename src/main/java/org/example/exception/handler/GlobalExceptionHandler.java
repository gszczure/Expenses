package org.example.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.response.ErrorResponse;
import org.example.exception.AccountNotFoundException;
import org.example.exception.CannotDeleteAccountException;
import org.example.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String UNEXPECTED_ERROR = "Unexpected error occurred";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception,
                                                                   HttpServletRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Validation error");

        log.warn("Validation error: {}", message);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException exception,
                                                                        HttpServletRequest request) {

        log.warn("Account not found: {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFoundException(TransactionNotFoundException exception,
                                                                            HttpServletRequest request) {

        log.warn("Transaction not found: {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(CannotDeleteAccountException.class)
    public ResponseEntity<ErrorResponse> handleCannotDeleteAccountException(CannotDeleteAccountException exception,
                                                                            HttpServletRequest request) {

        log.warn("Cannot delete account: {}", exception.getMessage());

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception,
                                                                               HttpServletRequest request) {
        log.error("Invalid request body", exception);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid transaction type",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {

        log.error("Unexpected error occurred", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                UNEXPECTED_ERROR,
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception,
                                                         HttpServletRequest request) {

        log.error("From date cannot be after to date", exception);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path) {

        ErrorResponse errorResponse = new ErrorResponse(
                ZonedDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }
}
