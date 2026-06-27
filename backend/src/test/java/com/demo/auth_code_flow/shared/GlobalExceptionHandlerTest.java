package com.demo.auth_code_flow.shared;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsMissingResourcesToConsistentNotFoundResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/missing");
        NoResourceFoundException exception =
                new NoResourceFoundException(HttpMethod.GET, "/missing", "static resource");

        ResponseEntity<ApiError> response = handler.handleNoResourceFound(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().path()).isEqualTo("/missing");
    }

    @Test
    void doesNotExposeIllegalStateExceptionMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        IllegalStateException exception = new IllegalStateException("internal diagnostic detail");

        ResponseEntity<ApiError> response = handler.handleIllegalState(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_STATE");
        assertThat(response.getBody().message()).isEqualTo("Invalid request state");
    }

    @Test
    void doesNotExposeSecurityExceptionMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects");
        SecurityException exception = new SecurityException("internal authorization detail");

        ResponseEntity<ApiError> response = handler.handleSecurityException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("ACCESS_DENIED");
        assertThat(response.getBody().message()).isEqualTo("Access denied");
    }
}
