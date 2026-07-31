package com.fiap.tech_challenge_fase2.infrastructure.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("Deve publicar evento com sucesso no RabbitMQ")
    void shouldPublishEventSuccessfully() {
        Object event = new Object();
        eventPublisher.publishEvent("routing.key", event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("routing.key"), eq(event));
    }

    @Test
    @DisplayName("Deve capturar excecao ao falhar a publicacao no RabbitMQ")
    void shouldHandleExceptionWhenPublishingFails() {
        Object event = new Object();
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("routing.key"), eq(event));

        eventPublisher.publishEvent("routing.key", event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("routing.key"), eq(event));
    }
}
