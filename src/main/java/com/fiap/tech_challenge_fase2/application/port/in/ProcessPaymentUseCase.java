package com.fiap.tech_challenge_fase2.application.port.in;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import java.math.BigDecimal;

public interface ProcessPaymentUseCase {
    Payment processPaymentForServiceOrder(String serviceOrderId, BigDecimal amount, String customerEmail);
    Payment getPaymentByServiceOrder(String serviceOrderId);
    Payment processWebhookNotification(String externalId, String action);
}
