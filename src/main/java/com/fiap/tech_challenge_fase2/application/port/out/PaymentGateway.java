package com.fiap.tech_challenge_fase2.application.port.out;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import java.math.BigDecimal;

public interface PaymentGateway {
    Payment createPayment(String serviceOrderId, BigDecimal amount, String customerEmail);
    Payment getPaymentStatus(String externalPaymentId);
}
