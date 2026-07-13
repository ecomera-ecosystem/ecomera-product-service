package com.ecomera.product.shared.common.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock(lenient = true)
    private HttpServletRequest request;

    @Mock
    private WebRequest webRequest;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        given(request.getRequestURI()).willReturn("/api/v1/test");
    }

    @Test
    void handleApiException_shouldReturnCorrectStatus() {
        ApiException ex = new ApiException("Test error", HttpStatus.BAD_REQUEST);

        ResponseEntity<ErrorResponse> response = handler.handleApiException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Test error");
    }

    @Test
    void handleApiException_shouldReturnNotFoundStatus() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product", "id", UUID.randomUUID());

        ResponseEntity<ErrorResponse> response = handler.handleApiException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleValidationException_shouldReturnBadRequestWithFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "email", "Invalid email format"));
        bindingResult.addError(new FieldError("test", "password", "Password too short"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().validationErrors()).containsKeys("email", "password");
        assertThat(response.getBody().validationErrors().get("email")).isEqualTo("Invalid email format");
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequest() {
        jakarta.validation.ConstraintViolationException ex =
                new jakarta.validation.ConstraintViolationException("Constraint violation", Collections.emptySet());

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleHttpMessageNotReadable_shouldReturnBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("Malformed JSON");
    }

    @Test
    void handleTypeMismatch_shouldReturnBadRequest() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", UUID.class, "id", null, null);

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("Invalid value 'abc'");
    }

    @Test
    void handleResourceNotFoundException_shouldReturnNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product", "id", UUID.randomUUID());

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("Product");
    }

    @Test
    void handleGenericException_shouldReturn500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).contains("Unexpected error");
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnConflict() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate entry");
        given(webRequest.getDescription(false)).willReturn("uri=/api/v1/products");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("Duplicate entry not allowed");
    }
}
