package com.fiap.tech_challenge_fase2.interfaces.controller;

import com.fiap.tech_challenge_fase2.application.port.in.ProcessPaymentUseCase;
import com.fiap.tech_challenge_fase2.domain.entity.Payment;
import com.fiap.tech_challenge_fase2.interfaces.dto.PaymentResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Endpoints para gerenciamento e processamento de pagamentos")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    public record CreatePaymentRequest(String serviceOrderId, BigDecimal amount, String customerEmail) {}

    @PostMapping
    @Operation(summary = "Gera a cobrança/pagamento PIX para uma Ordem de Serviço")
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody CreatePaymentRequest request) {
        Payment payment = processPaymentUseCase.processPaymentForServiceOrder(
                request.serviceOrderId(), request.amount(), request.customerEmail());
        return ResponseEntity.ok(PaymentResponseDTO.fromDomain(payment));
    }

    @GetMapping("/service-order/{serviceOrderId}")
    @Operation(summary = "Busca os detalhes do pagamento de uma Ordem de Serviço")
    public ResponseEntity<PaymentResponseDTO> getPaymentByServiceOrder(@PathVariable String serviceOrderId) {
        Payment payment = processPaymentUseCase.getPaymentByServiceOrder(serviceOrderId);
        return ResponseEntity.ok(PaymentResponseDTO.fromDomain(payment));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Recebe notificações de alteração de status do Mercado Pago")
    public ResponseEntity<Void> handleWebhook(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "action", required = false) String action) {
        
        if (dataId != null) {
            processPaymentUseCase.processWebhookNotification(dataId, action);
        }

        return ResponseEntity.ok().build();
    }
}
