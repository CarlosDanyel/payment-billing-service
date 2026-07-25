package com.fiap.tech_challenge_fase2.infrastructure.persistence.mapper;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.PaymentJpaEntity;

public class PaymentMapper {

    public static Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) return null;
        return new Payment(
                entity.getId(),
                entity.getServiceOrderId(),
                entity.getExternalId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getPaymentMethod(),
                entity.getQrCode(),
                entity.getQrCodeBase64(),
                entity.getTicketUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PaymentJpaEntity toEntity(Payment domain) {
        if (domain == null) return null;
        return new PaymentJpaEntity(
                domain.getId(),
                domain.getServiceOrderId(),
                domain.getExternalId(),
                domain.getAmount(),
                domain.getStatus(),
                domain.getPaymentMethod(),
                domain.getQrCode(),
                domain.getQrCodeBase64(),
                domain.getTicketUrl(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
