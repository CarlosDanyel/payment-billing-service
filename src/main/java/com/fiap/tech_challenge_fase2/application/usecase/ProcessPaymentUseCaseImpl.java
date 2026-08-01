package com.fiap.tech_challenge_fase2.application.usecase;

import com.fiap.tech_challenge_fase2.application.port.in.ProcessPaymentUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentGateway;
import com.fiap.tech_challenge_fase2.application.port.out.PaymentRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ResourceNotFoundException;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.RabbitMQConfig;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class ProcessPaymentUseCaseImpl implements ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentUseCaseImpl.class);

    private final PaymentRepositoryPort paymentRepository;
    private final PaymentGateway paymentGateway;
    private final EventPublisher eventPublisher;

    public ProcessPaymentUseCaseImpl(PaymentRepositoryPort paymentRepository,
                                       PaymentGateway paymentGateway,
                                       EventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Payment processPaymentForServiceOrder(String serviceOrderId, BigDecimal amount, String customerEmail) {
        return paymentRepository.findByServiceOrderId(serviceOrderId)
                .orElseGet(() -> {
                    Payment createdPayment = paymentGateway.createPayment(serviceOrderId, amount, customerEmail);
                    return paymentRepository.save(createdPayment);
                });
    }

    @Override
    public Payment getPaymentByServiceOrder(String serviceOrderId) {
        return paymentRepository.findByServiceOrderId(serviceOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para a OS: " + serviceOrderId));
    }

    @Override
    public Payment processWebhookNotification(String externalId, String action) {
        log.info("Processando notificação de webhook. ExternalId: {}, Action: {}", externalId, action);

        Payment updatedPaymentFromMp = paymentGateway.getPaymentStatus(externalId);

        Payment payment = paymentRepository.findByExternalId(externalId)
                .orElseGet(() -> paymentRepository.findByServiceOrderId(updatedPaymentFromMp.getServiceOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para externalId: " + externalId)));

        PaymentStatus finalStatus = updatedPaymentFromMp.getStatus();
        if (action != null && (action.contains("fail") || action.contains("reject"))) {
            finalStatus = PaymentStatus.REJECTED;
        }

        payment.updatePaymentDetails(
                updatedPaymentFromMp.getExternalId(),
                finalStatus,
                updatedPaymentFromMp.getQrCode(),
                updatedPaymentFromMp.getQrCodeBase64(),
                updatedPaymentFromMp.getTicketUrl()
        );

        Payment savedPayment = paymentRepository.save(payment);

        if (savedPayment.getStatus() == PaymentStatus.APPROVED) {
            log.info("Pagamento aprovado para a OS {}. Publicando evento Saga.", savedPayment.getServiceOrderId());
            eventPublisher.publishEvent(
                    RabbitMQConfig.ROUTING_KEY_PAY_APPROVED,
                    new ServiceOrderEvents.PaymentApprovedEvent(savedPayment.getServiceOrderId(), savedPayment.getExternalId())
            );
        } else if (savedPayment.getStatus() == PaymentStatus.REJECTED || savedPayment.getStatus() == PaymentStatus.CANCELLED) {
            log.info("Pagamento recusado para a OS {}. Publicando evento Saga de compensação/rollback.", savedPayment.getServiceOrderId());
            eventPublisher.publishEvent(
                    RabbitMQConfig.ROUTING_KEY_PAY_FAILED,
                    new ServiceOrderEvents.PaymentFailedEvent(savedPayment.getServiceOrderId(), "Pagamento " + savedPayment.getStatus())
            );
        }

        return savedPayment;
    }
}
