package com.fiap.tech_challenge_fase2.controller;

import com.fiap.tech_challenge_fase2.application.port.in.ProcessPaymentUseCase;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ResourceNotFoundException;
import com.fiap.tech_challenge_fase2.interfaces.controller.PaymentController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController")
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ProcessPaymentUseCase useCase;

    @Nested
    @DisplayName("POST /api/payments")
    class CreatePayment {

        @Test
        @DisplayName("Deve criar pagamento e retornar 200 com PaymentResponseDTO")
        void shouldCreatePayment() throws Exception {
            Payment payment = Payment.create("OS-001", new BigDecimal("299.90"));
            when(useCase.processPaymentForServiceOrder(eq("OS-001"), eq(new BigDecimal("299.90")), eq("cliente@test.com")))
                    .thenReturn(payment);

            String body = objectMapper.writeValueAsString(
                    new PaymentController.CreatePaymentRequest("OS-001", new BigDecimal("299.90"), "cliente@test.com"));

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.serviceOrderId").value("OS-001"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.paymentMethod").value("PIX"));
        }
    }

    @Nested
    @DisplayName("GET /api/payments/service-order/{id}")
    class GetPayment {

        @Test
        @DisplayName("Deve retornar 200 com detalhes do pagamento")
        void shouldReturnPayment() throws Exception {
            Payment payment = Payment.create("OS-001", new BigDecimal("500.00"));
            payment.updatePaymentDetails("EXT-777", PaymentStatus.APPROVED, "QR", "B64", "http://ticket.url");
            when(useCase.getPaymentByServiceOrder("OS-001")).thenReturn(payment);

            mockMvc.perform(get("/api/payments/service-order/OS-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.externalId").value("EXT-777"))
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando pagamento não encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            when(useCase.getPaymentByServiceOrder("OS-999"))
                    .thenThrow(new ResourceNotFoundException("Pagamento não encontrado para a OS: OS-999"));

            mockMvc.perform(get("/api/payments/service-order/OS-999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/payments/webhook")
    class Webhook {

        @Test
        @DisplayName("Deve processar webhook com data.id e retornar 200")
        void shouldProcessWebhook() throws Exception {
            Payment payment = Payment.create("OS-001", BigDecimal.TEN);
            payment.updatePaymentDetails("EXT-123", PaymentStatus.APPROVED, null, null, null);
            when(useCase.processWebhookNotification("EXT-123", "payment.updated")).thenReturn(payment);

            mockMvc.perform(post("/api/payments/webhook")
                            .param("data.id", "EXT-123")
                            .param("action", "payment.updated"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 200 mesmo sem data.id (ignora)")
        void shouldIgnoreWebhookWithoutDataId() throws Exception {
            mockMvc.perform(post("/api/payments/webhook")
                            .param("id", "123"))
                    .andExpect(status().isOk());
        }
    }
}
