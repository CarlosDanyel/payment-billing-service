package com.fiap.tech_challenge_fase2.infrastructure.persistence.mapper;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.PaymentJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    @Test
    @DisplayName("Deve mapear PaymentJpaEntity para Payment domain")
    void shouldMapEntityToDomain() {
        LocalDateTime now = LocalDateTime.now();
        PaymentJpaEntity entity = new PaymentJpaEntity(
                "pay-1", "so-1", "ext-1", new BigDecimal("150.00"),
                PaymentStatus.APPROVED, "PIX", "qr-code-str", "qr-base64",
                "http://ticket", now, now
        );

        Payment domain = PaymentMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo("pay-1");
        assertThat(domain.getServiceOrderId()).isEqualTo("so-1");
        assertThat(domain.getExternalId()).isEqualTo("ext-1");
        assertThat(domain.getAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(domain.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(domain.getPaymentMethod()).isEqualTo("PIX");
        assertThat(domain.getQrCode()).isEqualTo("qr-code-str");
        assertThat(domain.getQrCodeBase64()).isEqualTo("qr-base64");
        assertThat(domain.getTicketUrl()).isEqualTo("http://ticket");
        assertThat(domain.getCreatedAt()).isEqualTo(now);
        assertThat(domain.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Deve mapear Payment domain para PaymentJpaEntity")
    void shouldMapDomainToEntity() {
        LocalDateTime now = LocalDateTime.now();
        Payment domain = new Payment(
                "pay-2", "so-2", "ext-2", new BigDecimal("250.00"),
                PaymentStatus.PENDING, "PIX", "qr", "base64",
                "http://ticket2", now, now
        );

        PaymentJpaEntity entity = PaymentMapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("pay-2");
        assertThat(entity.getServiceOrderId()).isEqualTo("so-2");
        assertThat(entity.getExternalId()).isEqualTo("ext-2");
        assertThat(entity.getAmount()).isEqualTo(new BigDecimal("250.00"));
        assertThat(entity.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(entity.getPaymentMethod()).isEqualTo("PIX");
        assertThat(entity.getQrCode()).isEqualTo("qr");
        assertThat(entity.getQrCodeBase64()).isEqualTo("base64");
        assertThat(entity.getTicketUrl()).isEqualTo("http://ticket2");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Deve retornar null ao mapear objetos nulos")
    void shouldReturnNullForNullInputs() {
        assertThat(PaymentMapper.toDomain(null)).isNull();
        assertThat(PaymentMapper.toEntity(null)).isNull();
    }
}
