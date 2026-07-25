package com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter;

import com.fiap.tech_challenge_fase2.application.port.out.PaymentRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.mapper.PaymentMapper;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.SpringPaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final SpringPaymentRepository springRepository;

    public PaymentRepositoryAdapter(SpringPaymentRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Payment save(Payment payment) {
        var entity = PaymentMapper.toEntity(payment);
        var saved = springRepository.save(entity);
        return PaymentMapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return springRepository.findById(id).map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByServiceOrderId(String serviceOrderId) {
        return springRepository.findByServiceOrderId(serviceOrderId).map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByExternalId(String externalId) {
        return springRepository.findByExternalId(externalId).map(PaymentMapper::toDomain);
    }
}
