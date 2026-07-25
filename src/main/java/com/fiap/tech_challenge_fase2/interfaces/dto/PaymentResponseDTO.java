package com.fiap.tech_challenge_fase2.interfaces.dto;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        String id,
        String serviceOrderId,
        String externalId,
        BigDecimal amount,
        PaymentStatus status,
        String paymentMethod,
        String qrCode,
        String qrCodeBase64,
        String ticketUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentResponseDTO fromDomain(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getServiceOrderId(),
                payment.getExternalId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getQrCode(),
                payment.getQrCodeBase64(),
                payment.getTicketUrl(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
