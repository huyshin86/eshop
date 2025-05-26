package com.example.eshop.exception;

import com.example.eshop.model.dto.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // Custom error response for missing required request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.error("Missing required request parameter: {}", ex.getParameterName());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST ,request.getRequestURI());
        String errorMessage = String.format("Required parameter '%s' is missing", ex.getParameterName());
        response.addError(ex.getParameterName(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for type mismatch errors
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.error("Method argument type mismatch: Parameter '{}' failed to convert value '{}' to type '{}'",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST, request.getRequestURI());
        String errorMessage = String.format("Parameter '%s' has invalid format. Expected '%s'. Received '%s'.",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "a valid type",
                ex.getValue());
        response.addError("message", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for mismatch constraint in request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.error("Validation error occurred: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST ,request.getRequestURI());

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

    // Custom error response for mismatch constraint in request parameter, header, path variable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        logger.error("Constraint violation occurred: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST ,request.getRequestURI());

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String[] parts = fieldName.split("\\.");
            String field = parts[parts.length - 1];
            String message = violation.getMessage();
            logger.debug("Path: {}, Message: {}", field, message);
            response.addError(field, message);
        });

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for invalid format in request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.error("Message not readable: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST ,request.getRequestURI());
        response.addError("message", "Invalid request body format");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for failure authentication
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        logger.error("Authentication failed: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED ,request.getRequestURI());
        response.addError("message", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        logger.error("Response status exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse((HttpStatus) ex.getStatusCode(),request.getRequestURI());
        response.addError("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred: ", ex);
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR ,request.getRequestURI());
        response.addError("message", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Custom error response for user not found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerUserNotFound(UserNotFoundException ex, HttpServletRequest request){
        logger.error("User {} not found", ex.getId());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND ,request.getRequestURI());
        response.addError("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Custom error response for email already in use
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handlerEmailAlreadyInUse(EmailAlreadyInUseException ex, HttpServletRequest request){
        logger.error("Email already in use: {}", ex.getEmail());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST ,request.getRequestURI());
        response.addError("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for password mismatch in update new password
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest request) {
        logger.error("Password validation failed: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST ,request.getRequestURI());
        response.addError("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Custom error response for resource ownership
    @ExceptionHandler(ResourceOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleResourceOwnership(ResourceOwnershipException ex, HttpServletRequest request) {
        logger.error("Resource ownership violation: User {} attempted to access resource {}", 
                    ex.getUserId(), ex.getResourceId());
        ErrorResponse response = new ErrorResponse(HttpStatus.FORBIDDEN ,request.getRequestURI());
        response.addError("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Custom error response for access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(HttpStatus.FORBIDDEN ,request.getRequestURI());
        response.addError("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Custom error response for product not found
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerProductNotFoundException(ProductNotFoundException ex, HttpServletRequest request){
        logger.error("Product {} not found", ex.getProductId());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND ,request.getRequestURI());
        response.addError("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Custom error response for cart item not found
    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerCartItemNotFoundException(CartItemNotFoundException ex, HttpServletRequest request){
        logger.error("Product {} not found in user {}'s cart", ex.getProductId(), ex.getUserId());
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND ,request.getRequestURI());
        response.addError("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}