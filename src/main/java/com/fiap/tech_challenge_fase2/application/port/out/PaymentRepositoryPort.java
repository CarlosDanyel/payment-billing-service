package com.fiap.tech_challenge_fase2.application.port.out;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import java.util.Optional;

public interface PaymentRepositoryPort {
    Payment save(Payment payment);
    Optional<Payment> findById(String id);
    Optional<Payment> findByServiceOrderId(String serviceOrderId);
    Optional<Payment> findByExternalId(String externalId);
}
