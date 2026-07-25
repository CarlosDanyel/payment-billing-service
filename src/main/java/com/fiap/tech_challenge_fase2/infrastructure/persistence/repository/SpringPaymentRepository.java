package com.fiap.tech_challenge_fase2.infrastructure.persistence.repository;

import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringPaymentRepository extends JpaRepository<PaymentJpaEntity, String> {
    Optional<PaymentJpaEntity> findByServiceOrderId(String serviceOrderId);
    Optional<PaymentJpaEntity> findByExternalId(String externalId);
}
