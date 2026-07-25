package com.fiap.tech_challenge_fase2.usecase;

import com.fiap.tech_challenge_fase2.application.port.out.PaymentGateway;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.ProcessPaymentUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentUseCaseTest")
class ProcessPaymentUseCaseTest {

    @Mock private PaymentRepositoryPort paymentRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private EventPublisher eventPublisher;

    private ProcessPaymentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentUseCaseImpl(paymentRepository, paymentGateway, eventPublisher);
    }

    @Test
    @DisplayName("Deve criar novo pagamento quando não existir para a OS")
    void shouldCreatePayment() {
        when(paymentRepository.findByServiceOrderId("OS-123")).thenReturn(Optional.empty());
        Payment mockPayment = Payment.create("OS-123", new BigDecimal("150.00"));
        when(paymentGateway.createPayment("OS-123", new BigDecimal("150.00"), "cliente@test.com")).thenReturn(mockPayment);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment result = useCase.processPaymentForServiceOrder("OS-123", new BigDecimal("150.00"), "cliente@test.com");

        assertThat(result.getServiceOrderId()).isEqualTo("OS-123");
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve publicar evento de saga quando pagamento for aprovado via Webhook")
    void shouldPublishSagaEventOnApprovedWebhook() {
        Payment mockMp = Payment.create("OS-123", new BigDecimal("150.00"));
        mockMp.updatePaymentDetails("EXT-123", PaymentStatus.APPROVED, null, null, null);

        when(paymentGateway.getPaymentStatus("EXT-123")).thenReturn(mockMp);
        when(paymentRepository.findByExternalId("EXT-123")).thenReturn(Optional.of(mockMp));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment result = useCase.processWebhookNotification("EXT-123", "payment.updated");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        verify(eventPublisher, times(1)).publishEvent(anyString(), any());
    }
}
