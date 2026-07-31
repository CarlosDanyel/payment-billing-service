package com.fiap.tech_challenge_fase2.usecase;

import com.fiap.tech_challenge_fase2.application.port.out.PaymentGateway;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.ProcessPaymentUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ResourceNotFoundException;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentUseCaseImpl — Extended Tests")
class ProcessPaymentUseCaseExtendedTest {

    @Mock private PaymentRepositoryPort paymentRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private EventPublisher eventPublisher;

    private ProcessPaymentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentUseCaseImpl(paymentRepository, paymentGateway, eventPublisher);
    }

    @Nested
    @DisplayName("processPaymentForServiceOrder()")
    class ProcessPayment {

        @Test
        @DisplayName("Deve retornar pagamento existente sem criar novo")
        void shouldReturnExistingPayment() {
            Payment existing = Payment.create("OS-123", new BigDecimal("150.00"));
            when(paymentRepository.findByServiceOrderId("OS-123")).thenReturn(Optional.of(existing));

            Payment result = useCase.processPaymentForServiceOrder("OS-123", new BigDecimal("150.00"), "cliente@test.com");

            assertThat(result).isSameAs(existing);
            verify(paymentGateway, never()).createPayment(anyString(), any(), anyString());
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve criar pagamento com valor zero")
        void shouldCreatePaymentWithZeroAmount() {
            when(paymentRepository.findByServiceOrderId("OS-ZERO")).thenReturn(Optional.empty());
            Payment mock = Payment.create("OS-ZERO", BigDecimal.ZERO);
            when(paymentGateway.createPayment("OS-ZERO", BigDecimal.ZERO, "cliente@test.com")).thenReturn(mock);
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Payment result = useCase.processPaymentForServiceOrder("OS-ZERO", BigDecimal.ZERO, "cliente@test.com");

            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getPaymentByServiceOrder()")
    class GetPayment {

        @Test
        @DisplayName("Deve retornar pagamento existente")
        void shouldReturnExistingPayment() {
            Payment payment = Payment.create("OS-456", new BigDecimal("200.00"));
            when(paymentRepository.findByServiceOrderId("OS-456")).thenReturn(Optional.of(payment));

            Payment result = useCase.getPaymentByServiceOrder("OS-456");

            assertThat(result.getServiceOrderId()).isEqualTo("OS-456");
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando não encontrado")
        void shouldThrowWhenNotFound() {
            when(paymentRepository.findByServiceOrderId("OS-999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getPaymentByServiceOrder("OS-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("OS-999");
        }
    }

    @Nested
    @DisplayName("processWebhookNotification()")
    class ProcessWebhook {

        @Test
        @DisplayName("Deve publicar PaymentFailedEvent para pagamento REJECTED")
        void shouldPublishFailedEventOnRejected() {
            Payment mockMp = Payment.create("OS-RJ", new BigDecimal("300.00"));
            mockMp.updatePaymentDetails("EXT-RJ", PaymentStatus.REJECTED, null, null, null);

            when(paymentGateway.getPaymentStatus("EXT-RJ")).thenReturn(mockMp);
            when(paymentRepository.findByExternalId("EXT-RJ")).thenReturn(Optional.empty());
            when(paymentRepository.findByServiceOrderId("OS-RJ")).thenReturn(Optional.of(mockMp));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Payment result = useCase.processWebhookNotification("EXT-RJ", "payment.updated");

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.REJECTED);
            verify(eventPublisher).publishEvent(anyString(), any(com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents.PaymentFailedEvent.class));
        }

        @Test
        @DisplayName("Deve publicar PaymentFailedEvent para pagamento CANCELLED")
        void shouldPublishFailedEventOnCancelled() {
            Payment mockMp = Payment.create("OS-CC", new BigDecimal("100.00"));
            mockMp.updatePaymentDetails("EXT-CC", PaymentStatus.CANCELLED, null, null, null);

            when(paymentGateway.getPaymentStatus("EXT-CC")).thenReturn(mockMp);
            when(paymentRepository.findByExternalId("EXT-CC")).thenReturn(Optional.of(mockMp));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Payment result = useCase.processWebhookNotification("EXT-CC", null);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            verify(eventPublisher).publishEvent(anyString(), any(com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents.PaymentFailedEvent.class));
        }

        @Test
        @DisplayName("Não deve publicar evento para pagamento PENDING")
        void shouldNotPublishEventOnPending() {
            Payment mockMp = Payment.create("OS-PD", new BigDecimal("50.00"));
            mockMp.updatePaymentDetails("EXT-PD", PaymentStatus.PENDING, "QR", "B64", "URL");

            when(paymentGateway.getPaymentStatus("EXT-PD")).thenReturn(mockMp);
            when(paymentRepository.findByExternalId("EXT-PD")).thenReturn(Optional.of(mockMp));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            useCase.processWebhookNotification("EXT-PD", "payment.created");

            verify(eventPublisher, never()).publishEvent(anyString(), any());
        }

        @Test
        @DisplayName("Não deve publicar evento para pagamento REFUNDED")
        void shouldNotPublishEventOnRefunded() {
            Payment mockMp = Payment.create("OS-RF", new BigDecimal("50.00"));
            mockMp.updatePaymentDetails("EXT-RF", PaymentStatus.REFUNDED, null, null, null);

            when(paymentGateway.getPaymentStatus("EXT-RF")).thenReturn(mockMp);
            when(paymentRepository.findByExternalId("EXT-RF")).thenReturn(Optional.of(mockMp));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            useCase.processWebhookNotification("EXT-RF", "payment.updated");

            verify(eventPublisher, never()).publishEvent(anyString(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pagamento não encontrado por externalId")
        void shouldThrowWhenPaymentNotFound() {
            Payment mockMp = Payment.create("OS-404", BigDecimal.TEN);
            when(paymentGateway.getPaymentStatus("EXT-404")).thenReturn(mockMp);
            when(paymentRepository.findByExternalId("EXT-404")).thenReturn(Optional.empty());
            when(paymentRepository.findByServiceOrderId("OS-404")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.processWebhookNotification("EXT-404", null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("EXT-404");
        }
    }
}
