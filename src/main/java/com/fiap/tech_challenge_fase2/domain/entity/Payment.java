package com.fiap.tech_challenge_fase2.domain.entity;

import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final String id;
    private final String serviceOrderId;
    private String externalId;
    private final BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String qrCode;
    private String qrCodeBase64;
    private String ticketUrl;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment(String id, String serviceOrderId, String externalId, BigDecimal amount,
                   PaymentStatus status, String paymentMethod, String qrCode,
                   String qrCodeBase64, String ticketUrl,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.serviceOrderId = Objects.requireNonNull(serviceOrderId, "Service order ID is required");
        this.externalId = externalId;
        this.amount = Objects.requireNonNull(amount, "Amount is required");
        this.status = status != null ? status : PaymentStatus.PENDING;
        this.paymentMethod = paymentMethod;
        this.qrCode = qrCode;
        this.qrCodeBase64 = qrCodeBase64;
        this.ticketUrl = ticketUrl;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Payment create(String serviceOrderId, BigDecimal amount) {
        return new Payment(
                UUID.randomUUID().toString(),
                serviceOrderId,
                null,
                amount,
                PaymentStatus.PENDING,
                "PIX",
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public void updatePaymentDetails(String externalId, PaymentStatus status, String qrCode, String qrCodeBase64, String ticketUrl) {
        if (externalId != null) this.externalId = externalId;
        if (status != null) this.status = status;
        if (qrCode != null) this.qrCode = qrCode;
        if (qrCodeBase64 != null) this.qrCodeBase64 = qrCodeBase64;
        if (ticketUrl != null) this.ticketUrl = ticketUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getServiceOrderId() { return serviceOrderId; }
    public String getExternalId() { return externalId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getQrCode() { return qrCode; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public String getTicketUrl() { return ticketUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
