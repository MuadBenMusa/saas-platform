package com.demo.auth_code_flow.shared;

import com.demo.auth_code_flow.projects.DuplicateProjectNumberException;
import com.demo.auth_code_flow.projects.ProjectNotFoundException;
import com.demo.auth_code_flow.users.CurrentUserNotProvisionedException;
import com.demo.auth_code_flow.users.InactiveAppUserException;
import com.demo.auth_code_flow.users.NoActiveTenantMembershipException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found", request);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiError> handleProjectNotFound(
            ProjectNotFoundException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateProjectNumberException.class)
    public ResponseEntity<ApiError> handleDuplicateProjectNumber(
            DuplicateProjectNumberException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "PROJECT_NUMBER_EXISTS", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.collectingAndThen(
                        Collectors.joining("; "),
                        joined -> joined.isBlank() ? "Validation failed" : joined
                ));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request);
    }

    @ExceptionHandler(CurrentUserNotProvisionedException.class)
    public ResponseEntity<ApiError> handleCurrentUserNotProvisioned(
            CurrentUserNotProvisionedException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "CURRENT_USER_NOT_PROVISIONED", ex.getMessage(), request);
    }

    @ExceptionHandler(InactiveAppUserException.class)
    public ResponseEntity<ApiError> handleInactiveAppUser(
            InactiveAppUserException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "APP_USER_INACTIVE", ex.getMessage(), request);
    }

    @ExceptionHandler(NoActiveTenantMembershipException.class)
    public ResponseEntity<ApiError> handleNoActiveTenantMembership(
            NoActiveTenantMembershipException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "NO_ACTIVE_TENANT_MEMBERSHIP", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_STATE", "Invalid request state", request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleSecurityException(
            SecurityException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error while handling {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error",
                request
        );
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }
}
