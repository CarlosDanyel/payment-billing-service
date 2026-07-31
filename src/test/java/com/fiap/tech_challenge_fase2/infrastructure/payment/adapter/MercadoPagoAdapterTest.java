package com.fiap.tech_challenge_fase2.infrastructure.payment.adapter;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercadoPagoAdapterTest {

    private MercadoPagoAdapter mercadoPagoAdapter;

    @BeforeEach
    void setUp() {
        mercadoPagoAdapter = new MercadoPagoAdapter("TEST-TOKEN", "http://localhost:8081/api/payments/webhook");
    }

    @Test
    @DisplayName("Deve criar pagamento PIX em modo mock com token de teste")
    void shouldCreatePaymentInMockMode() {
        Payment payment = mercadoPagoAdapter.createPayment("SO-123", new BigDecimal("100.00"), "cliente@email.com");

        assertThat(payment).isNotNull();
        assertThat(payment.getServiceOrderId()).isEqualTo("SO-123");
        assertThat(payment.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getExternalId()).startsWith("MP-MOCK-");
        assertThat(payment.getQrCode()).isNotNull();
        assertThat(payment.getQrCodeBase64()).isNotNull();
        assertThat(payment.getTicketUrl()).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar status de pagamento em modo mock para ID mock ou nulo ou nao numerico")
    void shouldGetPaymentStatusInMockMode() {
        Payment p1 = mercadoPagoAdapter.getPaymentStatus("MP-MOCK-123");
        assertThat(p1.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        Payment p2 = mercadoPagoAdapter.getPaymentStatus(null);
        assertThat(p2.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        Payment p3 = mercadoPagoAdapter.getPaymentStatus("invalid-id");
        assertThat(p3.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("Deve reconhecer tokens nulos e em branco como mock token")
    void shouldRecognizeNullAndBlankTokensAsMock() {
        MercadoPagoAdapter nullTokenAdapter = new MercadoPagoAdapter(null, "http://localhost:8081");
        Payment p1 = nullTokenAdapter.createPayment("SO-NULL", BigDecimal.TEN, "a@b.com");
        assertThat(p1.getExternalId()).startsWith("MP-MOCK-");

        MercadoPagoAdapter blankTokenAdapter = new MercadoPagoAdapter("   ", "http://localhost:8081");
        Payment p2 = blankTokenAdapter.createPayment("SO-BLANK", BigDecimal.TEN, "a@b.com");
        assertThat(p2.getExternalId()).startsWith("MP-MOCK-");
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar criar pagamento real com token customizado invalido")
    void shouldThrowExceptionOnRealCreatePaymentWithInvalidToken() {
        MercadoPagoAdapter realAdapter = new MercadoPagoAdapter("SOME-CUSTOM-TOKEN", "http://localhost:8081/api/payments/webhook");

        assertThatThrownBy(() -> realAdapter.createPayment("SO-999", new BigDecimal("50.00"), "test@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha na comunicação com o Mercado Pago");
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar buscar pagamento real com token customizado invalido")
    void shouldThrowExceptionOnRealGetPaymentStatusWithInvalidToken() {
        MercadoPagoAdapter realAdapter = new MercadoPagoAdapter("SOME-CUSTOM-TOKEN", "http://localhost:8081/api/payments/webhook");

        assertThatThrownBy(() -> realAdapter.getPaymentStatus("123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao consultar pagamento no Mercado Pago");
    }

    @ParameterizedTest
    @CsvSource({
            "approved, APPROVED",
            "rejected, REJECTED",
            "cancelled, CANCELLED",
            "refunded, REFUNDED",
            "charged_back, REFUNDED",
            "in_process, PENDING",
            "unknown, PENDING"
    })
    @DisplayName("Deve mapear corretamente os status do Mercado Pago")
    void shouldMapStatus(String inputStatus, PaymentStatus expectedStatus) {
        assertThat(mercadoPagoAdapter.mapStatus(inputStatus)).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Deve mapear status nulo para PENDING")
    void shouldMapNullStatusToPending() {
        assertThat(mercadoPagoAdapter.mapStatus(null)).isEqualTo(PaymentStatus.PENDING);
    }
}
