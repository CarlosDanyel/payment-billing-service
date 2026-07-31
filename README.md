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

## Saga Pattern: Participação na Coreografia

Este serviço é um participante do **Saga Pattern Coreografado** (Choreographed Saga).

### Papel no Saga

O `payment-billing-service` atua como **participante reativo** no fluxo transacional:

1. **Não inicia o Saga** — apenas reage a solicitações de pagamento via API REST
2. **Gera cobrança PIX** via Mercado Pago ao receber uma requisição `POST /api/payments`
3. **Processa webhooks** do Mercado Pago (`POST /api/payments/webhook`) com atualização de status
4. **Publica eventos de conclusão** que acionam o próximo passo ou rollback:
   - `PaymentApprovedEvent` → dispara entrega da OS no `ordem-de-service`
   - `PaymentFailedEvent` → dispara compensação (OS → CANCELED)

### Compensação (Rollback)

Em caso de falha no pagamento (recusado, cancelado, expirado), este serviço publica `PaymentFailedEvent`. O `ordem-de-service` então executa a ação compensatória: transiciona a Ordem de Serviço para `CANCELED`. Isso garante consistência eventual sem a necessidade de um orquestrador central.

### Eventos Publicados

| Evento | Routing Key | Quando |
|---|---|---|
| `PaymentApprovedEvent` | `payment.approved` | Pagamento confirmado via webhook |
| `PaymentFailedEvent` | `payment.failed` | Pagamento rejeitado ou cancelado |

---

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

---

## Como Iniciar com Kubernetes

📖 **[GUIA_RUN.md — Guia Completo de Execução](../GUIA_RUN.md)**

```bash
docker build -t payment-billing-service:latest .
kubectl apply -k k8s/
kubectl port-forward svc/payment-billing-service 8081:80 -n fiap-oficina
```

---

## Testes e Cobertura

```bash
./mvnw clean verify      # Testes + JaCoCo ≥ 80%
open target/site/jacoco/index.html
```

| Pipeline | Trigger | O que faz |
|---|---|---|
| **CI** | Push + PR | Build + Testes + JaCoCo + SonarQube |
| **CD** | Push `main` | Docker build + Deploy K8s + Rollback |

---

## Coleção Postman

[postman_collection.json](./postman_collection.json) — Fluxo completo do Saga: Happy Path (pagamento PIX → aprovação webhook → OS entregue) e Rollback (pagamento recusado → compensação OS cancelada).
