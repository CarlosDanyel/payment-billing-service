package com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter;

import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.domain.enums.PaymentStatus;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.PaymentJpaEntity;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.SpringPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRepositoryAdapterTest {

    @Mock
    private SpringPaymentRepository springRepository;

    @InjectMocks
    private PaymentRepositoryAdapter repositoryAdapter;

    private Payment domainPayment;
    private PaymentJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        domainPayment = new Payment(
                "pay-1", "so-1", "ext-1", new BigDecimal("100.00"),
                PaymentStatus.PENDING, "PIX", "qr", "base64", "url", now, now
        );
        jpaEntity = new PaymentJpaEntity(
                "pay-1", "so-1", "ext-1", new BigDecimal("100.00"),
                PaymentStatus.PENDING, "PIX", "qr", "base64", "url", now, now
        );
    }

    @Test
    @DisplayName("Deve salvar o pagamento no repositorio JPA")
    void shouldSavePayment() {
        when(springRepository.save(any(PaymentJpaEntity.class))).thenReturn(jpaEntity);

        Payment result = repositoryAdapter.save(domainPayment);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("pay-1");
        verify(springRepository).save(any(PaymentJpaEntity.class));
    }

    @Test
    @DisplayName("Deve buscar pagamento por ID")
    void shouldFindById() {
        when(springRepository.findById("pay-1")).thenReturn(Optional.of(jpaEntity));

        Optional<Payment> result = repositoryAdapter.findById("pay-1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("pay-1");
    }

    @Test
    @DisplayName("Deve buscar pagamento por serviceOrderId")
    void shouldFindByServiceOrderId() {
        when(springRepository.findByServiceOrderId("so-1")).thenReturn(Optional.of(jpaEntity));

        Optional<Payment> result = repositoryAdapter.findByServiceOrderId("so-1");

        assertThat(result).isPresent();
        assertThat(result.get().getServiceOrderId()).isEqualTo("so-1");
    }

    @Test
    @DisplayName("Deve buscar pagamento por externalId")
    void shouldFindByExternalId() {
        when(springRepository.findByExternalId("ext-1")).thenReturn(Optional.of(jpaEntity));

        Optional<Payment> result = repositoryAdapter.findByExternalId("ext-1");

        assertThat(result).isPresent();
        assertThat(result.get().getExternalId()).isEqualTo("ext-1");
    }
}
