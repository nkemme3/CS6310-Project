package edu.gatech.cs6310.powergrid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import edu.gatech.cs6310.powergrid.error.ApiError;
import edu.gatech.cs6310.powergrid.error.ErrorCode;
import edu.gatech.cs6310.powergrid.error.SystemError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SystemError.class)
    public ResponseEntity<ApiError> handleSystemError(SystemError ex) {
        ApiError body = ApiError.from(ex);
        return ResponseEntity.status(HttpStatus.valueOf(ex.code().httpStatus())).body(body);
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ApiError> handleBadInput(Exception ex) {
        SystemError wrapped = SystemError.invalidCommand(
            ex.getMessage() == null ? "Request could not be parsed." : ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.from(wrapped));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        SystemError wrapped = new SystemError(
            ErrorCode.FORBIDDEN,
            "You do not have permission to perform this action.",
            null,
            "Ask an administrator for the required role."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.from(wrapped));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException ex) {
        SystemError wrapped = new SystemError(
            ErrorCode.UNAUTHORIZED,
            ex.getMessage() == null ? "Authentication is required." : ex.getMessage(),
            null,
            "POST /api/auth/login to obtain a bearer token."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.from(wrapped));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        SystemError wrapped = new SystemError(
            ErrorCode.INVALID_STATE,
            "Unexpected error: " + ex.getClass().getSimpleName()
                + (ex.getMessage() == null ? "" : " — " + ex.getMessage()),
            null,
            "Contact support if this persists."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.from(wrapped));
    }
}
