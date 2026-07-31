package com.fiap.tech_challenge_fase2.infrastructure.payment.adapter;

import com.fiap.tech_challenge_fase2.application.port.out.PaymentGateway;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.PaymentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MercadoPagoAdapter implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAdapter.class);

    private final String accessToken;
    private final String notificationUrl;

    private boolean isMockToken() {
        return accessToken == null || accessToken.isBlank() || accessToken.startsWith("TEST-") || "TEST-TOKEN".equals(accessToken);
    }

    public MercadoPagoAdapter(
            @Value("${mercadopago.access-token:TEST-TOKEN}") String accessToken,
            @Value("${mercadopago.notification-url:http://localhost:8080/api/payments/webhook}") String notificationUrl) {
        this.accessToken = accessToken;
        this.notificationUrl = notificationUrl;
        if (!isMockToken()) {
            MercadoPagoConfig.setAccessToken(accessToken);
        }
    }

    @Override
    public Payment createPayment(String serviceOrderId, BigDecimal amount, String customerEmail) {
        Payment domainPayment = Payment.create(serviceOrderId, amount);

        if (isMockToken()) {
            log.warn("Mercado Pago Access Token não configurado ou em ambiente mock. Retornando dados simulados de PIX.");
            domainPayment.updatePaymentDetails(
                    "MP-MOCK-" + System.currentTimeMillis(),
                    PaymentStatus.PENDING,
                    "00020126580014br.gov.bcb.pix0136123e4567-e89b-12d3-a456-426614174000520400005303986540510.005802BR5913Oficina Mecanica6008SAO PAULO62070503***6304E2CA",
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
                    "https://www.mercadopago.com.br/payments/ticket/option"
            );
            return domainPayment;
        }

        try {
            PaymentClient client = new PaymentClient();

            PaymentPayerRequest payer = PaymentPayerRequest.builder()
                    .email(customerEmail != null ? customerEmail : "cliente@oficina.com")
                    .build();

            PaymentCreateRequest createRequest = PaymentCreateRequest.builder()
                    .transactionAmount(amount)
                    .description("Pagamento OS #" + serviceOrderId)
                    .paymentMethodId("pix")
                    .notificationUrl(notificationUrl)
                    .payer(payer)
                    .build();

            com.mercadopago.resources.payment.Payment mpPayment = client.create(createRequest);

            PaymentStatus status = mapStatus(mpPayment.getStatus());
            String qrCode = null;
            String qrCodeBase64 = null;
            String ticketUrl = null;

            if (mpPayment.getPointOfInteraction() != null &&
                mpPayment.getPointOfInteraction().getTransactionData() != null) {
                qrCode = mpPayment.getPointOfInteraction().getTransactionData().getQrCode();
                qrCodeBase64 = mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
                ticketUrl = mpPayment.getPointOfInteraction().getTransactionData().getTicketUrl();
            }

            domainPayment.updatePaymentDetails(
                    String.valueOf(mpPayment.getId()),
                    status,
                    qrCode,
                    qrCodeBase64,
                    ticketUrl
            );

            log.info("Pagamento PIX criado com sucesso no Mercado Pago. ID: {}", mpPayment.getId());
            return domainPayment;
        } catch (Exception e) {
            log.error("Erro ao criar pagamento no Mercado Pago: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na comunicação com o Mercado Pago: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment getPaymentStatus(String externalPaymentId) {
        if (isMockToken() || externalPaymentId == null || externalPaymentId.startsWith("MP-MOCK-") || !externalPaymentId.matches("\\d+")) {
            log.warn("Mercado Pago em modo mock. Simulando aprovação de pagamento.");
            Payment mock = Payment.create("mock-so-id", BigDecimal.TEN);
            mock.updatePaymentDetails(externalPaymentId != null ? externalPaymentId : "MP-MOCK-123", PaymentStatus.APPROVED, null, null, null);
            return mock;
        }

        try {
            PaymentClient client = new PaymentClient();
            Long paymentId = Long.parseLong(externalPaymentId);
            com.mercadopago.resources.payment.Payment mpPayment = client.get(paymentId);

            Payment payment = Payment.create(
                    mpPayment.getExternalReference() != null ? mpPayment.getExternalReference() : "unknown",
                    mpPayment.getTransactionAmount()
            );

            PaymentStatus status = mapStatus(mpPayment.getStatus());
            payment.updatePaymentDetails(
                    String.valueOf(mpPayment.getId()),
                    status,
                    null, null, null
            );

            return payment;
        } catch (Exception e) {
            log.error("Erro ao buscar status do pagamento no Mercado Pago: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao consultar pagamento no Mercado Pago: " + e.getMessage(), e);
        }
    }

    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded", "charged_back" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }
}
