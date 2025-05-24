package com.example.eshop.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.error("Validation error occurred: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(request.getRequestURI());

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName;
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                fieldName = error.getObjectName();
            }
            String errorMessage = error.getDefaultMessage();
            logger.debug("Field: {}, Error: {}", fieldName, errorMessage);
            response.addError(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for mismatch constraint in request dto
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        logger.error("Constraint violation occurred: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(request.getRequestURI());

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            logger.debug("Path: {}, Message: {}", fieldName, message);
            response.addError(fieldName, message);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.error("Message not readable: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("message", "Invalid request body format");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for failure authentication
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        logger.error("Authentication failed: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("auth", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        logger.error("Response status exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("status", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred: ", ex);
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Custom error response for email already in use
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handlerEmailAlreadyInUse(EmailAlreadyInUseException ex, HttpServletRequest request){
        logger.error("Email already in use: {}", ex.getEmail());
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for password mismatch
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest request) {
        logger.error("Password validation failed: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("password", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for resource ownership
    @ExceptionHandler(ResourceOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleResourceOwnership(ResourceOwnershipException ex, HttpServletRequest request) {
        logger.error("Resource ownership violation: User {} attempted to access resource {}", 
                    ex.getUserId(), ex.getResourceId());
        
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("message", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(response);
    }

    // Custom error response for access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(request.getRequestURI());
        response.addError("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }
}