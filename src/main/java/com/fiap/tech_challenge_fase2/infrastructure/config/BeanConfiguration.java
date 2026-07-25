package com.fiap.tech_challenge_fase2.infrastructure.config;

import com.fiap.tech_challenge_fase2.application.port.in.ProcessPaymentUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentGateway;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.ProcessPaymentUseCaseImpl;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter.PaymentRepositoryAdapter;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.SpringPaymentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public PaymentRepositoryPort paymentRepositoryPort(SpringPaymentRepository springPaymentRepository) {
        return new PaymentRepositoryAdapter(springPaymentRepository);
    }

    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(
            PaymentRepositoryPort paymentRepositoryPort,
            PaymentGateway paymentGateway,
            com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher eventPublisher) {
        return new ProcessPaymentUseCaseImpl(paymentRepositoryPort, paymentGateway, eventPublisher);
    }
}
