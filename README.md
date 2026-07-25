# Payment & Billing Microservice (`payment-billing-service`)

Serviço responsável pela geração de cobranças, processamento de pagamentos integrados ao Mercado Pago (PIX) e recebimento de webhooks de notificação financeira.

---

## Arquitetura do Serviço

Construído sob a Clean Architecture garantindo isolamento total do domínio financeiro:

- **Domain Layer (`domain`)**: Entidade `Payment` com estados de pagamento (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`).
- **Application Layer (`application`)**: Caso de Uso `ProcessPaymentUseCase` para gerenciamento da cobrança e notificação Webhook.
- **Infrastructure Layer (`infrastructure`)**: Adaptador Mercado Pago SDK, persistência em MySQL exclusivo (`payment_db`) e emissão dos eventos de Saga (`PaymentApprovedEvent` e `PaymentFailedEvent`) via RabbitMQ.

```text
com.fiap.tech_challenge_fase2/
├── application/       # Use Cases, Ports (In/Out), DTOs
├── domain/            # Payment Entity, Enums, Value Objects
└── infrastructure/    # Mercado Pago Gateway, MySQL JPA, RabbitMQ Saga Publisher
```

---

## Coleção do Postman

O arquivo da coleção de chamadas da API de Pagamentos está disponível em:
- [postman_collection.json](./postman_collection.json)

---

## Como Executar o Microsserviço Localmente

### Pré-requisitos:
- Java 17 e Maven instalados.
- MySQL rodando na porta `3308` e RabbitMQ na porta `5672`.

### Comandos:

```bash
# Executar testes unitários e de integração
./mvnw clean test

# Subir a aplicação na porta 8081
./mvnw spring-boot:run
```

- **Swagger UI**: http://localhost:8081/swagger-ui.html
