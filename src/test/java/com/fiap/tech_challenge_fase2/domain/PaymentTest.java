package com.fiap.tech_challenge_fase2.domain;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment Domain Entity")
class PaymentTest {

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Deve criar Payment com status PENDING")
        void shouldCreateWithPendingStatus() {
            Payment payment = Payment.create("OS-001", new BigDecimal("299.90"));

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getServiceOrderId()).isEqualTo("OS-001");
            assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("299.90"));
            assertThat(payment.getPaymentMethod()).isEqualTo("PIX");
            assertThat(payment.getId()).isNotBlank();
            assertThat(payment.getCreatedAt()).isNotNull();
            assertThat(payment.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve gerar IDs diferentes para cada criação")
        void shouldGenerateUniqueIds() {
            Payment p1 = Payment.create("OS-A", BigDecimal.ONE);
            Payment p2 = Payment.create("OS-B", BigDecimal.TEN);

            assertThat(p1.getId()).isNotEqualTo(p2.getId());
        }

        @Test
        @DisplayName("Deve falhar ao criar com serviceOrderId nulo")
        void shouldFailOnNullServiceOrderId() {
            assertThatThrownBy(() -> Payment.create(null, BigDecimal.TEN))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Deve falhar ao criar com amount nulo")
        void shouldFailOnNullAmount() {
            assertThatThrownBy(() -> Payment.create("OS-001", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("updatePaymentDetails()")
    class UpdatePaymentDetails {

        @Test
        @DisplayName("Deve atualizar todos os detalhes do pagamento")
        void shouldUpdateAllDetails() {
            Payment payment = Payment.create("OS-001", new BigDecimal("150.00"));

            payment.updatePaymentDetails("EXT-999", PaymentStatus.APPROVED,
                    "QR_CODE", "QR_BASE64", "https://ticket.url");

            assertThat(payment.getExternalId()).isEqualTo("EXT-999");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getQrCode()).isEqualTo("QR_CODE");
            assertThat(payment.getQrCodeBase64()).isEqualTo("QR_BASE64");
            assertThat(payment.getTicketUrl()).isEqualTo("https://ticket.url");
        }

        @Test
        @DisplayName("Não deve sobrescrever campos com null")
        void shouldNotOverwriteNullFields() {
            Payment payment = Payment.create("OS-001", new BigDecimal("150.00"));
            payment.updatePaymentDetails("EXT-123", PaymentStatus.PENDING, "QR", null, null);

            assertThat(payment.getExternalId()).isEqualTo("EXT-123");
            assertThat(payment.getQrCode()).isEqualTo("QR");
            assertThat(payment.getQrCodeBase64()).isNull();
            assertThat(payment.getTicketUrl()).isNull();
        }

        @Test
        @DisplayName("Deve atualizar o updatedAt após updatePaymentDetails")
        void shouldUpdateTimestampOnUpdatePaymentDetails() {
            Payment payment = Payment.create("OS-001", BigDecimal.ONE);
            LocalDateTime before = payment.getUpdatedAt();

            payment.updatePaymentDetails("EXT-1", null, null, null, null);

            assertThat(payment.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Deve atualizar apenas o status")
        void shouldUpdateOnlyStatus() {
            Payment payment = Payment.create("OS-001", new BigDecimal("200.00"));

            payment.updateStatus(PaymentStatus.APPROVED);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getServiceOrderId()).isEqualTo("OS-001"); // unchanged
        }

        @Test
        @DisplayName("Deve atualizar o updatedAt após updateStatus")
        void shouldUpdateTimestampOnUpdateStatus() {
            Payment payment = Payment.create("OS-001", BigDecimal.ONE);
            LocalDateTime before = payment.getUpdatedAt();

            payment.updateStatus(PaymentStatus.REJECTED);

            assertThat(payment.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class Equality {

        @Test
        @DisplayName("Deve considerar pagamentos com mesmo ID como iguais")
        void shouldBeEqualBySameId() {
            Payment p1 = new Payment("ID-1", "OS-A", null, BigDecimal.ONE,
                    PaymentStatus.PENDING, "PIX", null, null, null,
                    LocalDateTime.now(), LocalDateTime.now());
            Payment p2 = new Payment("ID-1", "OS-B", null, BigDecimal.TEN,
                    PaymentStatus.APPROVED, null, null, null, null,
                    LocalDateTime.now(), LocalDateTime.now());

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("Deve considerar pagamentos com IDs diferentes como diferentes")
        void shouldBeDifferentById() {
            Payment p1 = Payment.create("OS-001", BigDecimal.ONE);
            Payment p2 = Payment.create("OS-001", BigDecimal.ONE);

            assertThat(p1).isNotEqualTo(p2);
        }
    }
}
