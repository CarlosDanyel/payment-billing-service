package com.fiap.tech_challenge_fase2.bdd;

import com.fiap.tech_challenge_fase2.application.port.in.ProcessPaymentUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentGateway;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.ProcessPaymentUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ResourceNotFoundException;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PaymentBillingSagaSteps {

    private PaymentRepositoryPort paymentRepository;
    private PaymentGateway paymentGateway;
    private EventPublisher eventPublisher;
    private ProcessPaymentUseCase useCase;

    private Payment actualPayment;
    private Exception actualException;
    private final Map<String, Payment> paymentStore = new HashMap<>();

    @Before
    public void setup() {
        paymentRepository = mock(PaymentRepositoryPort.class);
        paymentGateway = mock(PaymentGateway.class);
        eventPublisher = mock(EventPublisher.class);
        useCase = new ProcessPaymentUseCaseImpl(paymentRepository, paymentGateway, eventPublisher);
        actualPayment = null;
        actualException = null;
        paymentStore.clear();

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ────────────────── Contexto ──────────────────

    @Dado("que o sistema de pagamento está operacional")
    public void sistemaOperacional() {
        // Configuração mínima — os mocks já estão prontos
    }

    @Dado("que existe um pagamento para a OS {string}")
    public void existePagamento(String serviceOrderId) {
        Payment payment = Payment.create(serviceOrderId, new BigDecimal("200.00"));
        paymentStore.put(serviceOrderId, payment);
        when(paymentRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.of(payment));
    }

    @Dado("que existe um pagamento PENDING para a OS {string}")
    public void existePagamentoPending(String serviceOrderId) {
        Payment payment = Payment.create(serviceOrderId, new BigDecimal("300.00"));
        paymentStore.put(serviceOrderId, payment);
        when(paymentRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.of(payment));
        when(paymentRepository.findByExternalId(anyString())).thenReturn(Optional.of(payment));
    }

    // ────────────────── Quando ──────────────────

    @Quando("eu solicito a criação de um pagamento para a OS {string} no valor de R$ {double}")
    public void solicitoCriacaoPagamento(String serviceOrderId, double amount) {
        BigDecimal amt = BigDecimal.valueOf(amount);
        when(paymentRepository.findByServiceOrderId(serviceOrderId)).thenReturn(Optional.empty());
        Payment mockGatewayPayment = Payment.create(serviceOrderId, amt);
        when(paymentGateway.createPayment(eq(serviceOrderId), eq(amt), anyString())).thenReturn(mockGatewayPayment);

        actualPayment = useCase.processPaymentForServiceOrder(serviceOrderId, amt, "cliente@bdd.com");
    }

    @Quando("eu consulto o pagamento da OS {string}")
    public void consultoPagamento(String serviceOrderId) {
        try {
            actualPayment = useCase.getPaymentByServiceOrder(serviceOrderId);
        } catch (ResourceNotFoundException e) {
            actualException = e;
        }
    }

    @Quando("o webhook do Mercado Pago notifica aprovação do pagamento {string}")
    public void webhookAprovaPagamento(String externalId) {
        Payment mpPayment = Payment.create("SO-003", new BigDecimal("300.00"));
        mpPayment.updatePaymentDetails(externalId, PaymentStatus.APPROVED, null, null, null);

        when(paymentGateway.getPaymentStatus(externalId)).thenReturn(mpPayment);
        when(paymentRepository.findByExternalId(externalId)).thenReturn(Optional.of(paymentStore.get("SO-003")));

        actualPayment = useCase.processWebhookNotification(externalId, "payment.updated");
    }

    @Quando("o webhook do Mercado Pago notifica rejeição do pagamento {string}")
    public void webhookRejeitaPagamento(String externalId) {
        Payment mpPayment = Payment.create("SO-004", new BigDecimal("300.00"));
        mpPayment.updatePaymentDetails(externalId, PaymentStatus.REJECTED, null, null, null);

        when(paymentGateway.getPaymentStatus(externalId)).thenReturn(mpPayment);
        when(paymentRepository.findByExternalId(externalId)).thenReturn(Optional.of(paymentStore.get("SO-004")));

        actualPayment = useCase.processWebhookNotification(externalId, "payment.updated");
    }

    // ────────────────── Então ──────────────────

    @Entao("o pagamento deve ser criado com status {string}")
    public void pagamentoStatus(String expectedStatus) {
        assertThat(actualPayment.getStatus().name()).isEqualTo(expectedStatus);
    }

    @Entao("o método de pagamento deve ser {string}")
    public void metodoPagamento(String expectedMethod) {
        assertThat(actualPayment.getPaymentMethod()).isEqualTo(expectedMethod);
    }

    @Entao("o ID da OS no pagamento deve ser {string}")
    public void idDaOs(String expectedServiceOrderId) {
        assertThat(actualPayment.getServiceOrderId()).isEqualTo(expectedServiceOrderId);
    }

    @Entao("o pagamento deve ser retornado com sucesso")
    public void pagamentoRetornadoComSucesso() {
        assertThat(actualException).isNull();
        assertThat(actualPayment).isNotNull();
    }

    @Entao("o status do pagamento deve ser atualizado para {string}")
    public void statusAtualizado(String expectedStatus) {
        assertThat(actualPayment.getStatus().name()).isEqualTo(expectedStatus);
    }

    @Entao("um evento PaymentApprovedEvent deve ser publicado no RabbitMQ")
    public void eventoApprovedPublicado() {
        verify(eventPublisher, atLeastOnce()).publishEvent(anyString(), any());
    }

    @Entao("um evento PaymentFailedEvent deve ser publicado no RabbitMQ")
    public void eventoFailedPublicado() {
        verify(eventPublisher, atLeastOnce()).publishEvent(anyString(), any());
    }

    @Entao("o sistema deve retornar status 404")
    public void status404() {
        assertThat(actualException)
                .isNotNull()
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
