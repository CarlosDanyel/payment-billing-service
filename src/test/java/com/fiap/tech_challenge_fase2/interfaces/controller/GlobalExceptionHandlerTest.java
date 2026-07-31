package com.fiap.tech_challenge_fase2.interfaces.controller;

import com.fiap.tech_challenge_fase2.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException com 404 NOT_FOUND")
    void shouldHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso nao encontrado");

        ProblemDetail problem = exceptionHandler.handleResourceNotFound(ex);

        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getDetail()).isEqualTo("Recurso nao encontrado");
        assertThat(problem.getTitle()).isEqualTo("Recurso não encontrado");
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com 400 BAD_REQUEST")
    void shouldHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "erro de validacao");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail problem = exceptionHandler.handleValidation(ex);

        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Dados inválidos");
    }

    @Test
    @DisplayName("Deve tratar Exception generica com 500 INTERNAL_SERVER_ERROR")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Erro inesperado");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/payments");

        ProblemDetail problem = exceptionHandler.handleGeneric(ex, request);

        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Erro Interno");
    }
}
