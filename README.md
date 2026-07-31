# Payment & Billing Microservice (`payment-billing-service`)

Serviço responsável pela geração de cobranças, processamento de pagamentos integrados ao Mercado Pago (PIX) e recebimento de webhooks de notificação financeira no fluxo da oficina mecânica.

> 🚀 **Inicialização do Projeto e Infraestrutura**:
> A instrução completa de inicialização, infraestrutura e execução da solução está documentada no repositório central **[oficina-infra](https://github.com/CarlosDanyel/oficina-infra)**.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem & Framework**: Java 17, Spring Boot 3.3.5
- **Persistência de Dados**: Spring Data JPA, MySQL 8, Flyway Migrations
- **Mensageria & Saga**: RabbitMQ (AMQP)
- **Integração Financeira**: Mercado Pago SDK Java
- **Documentação de API**: OpenAPI 3 / Swagger UI
- **Testes & Cobertura**: JUnit 5, Mockito, JaCoCo, Cucumber BDD
- **Containerização & Orquestração**: Docker, Kubernetes (Kustomize)

---

## 🏛️ Arquitetura do Serviço

Construído sob os princípios da **Clean Architecture** (Ports and Adapters), garantindo o isolamento total do domínio financeiro:

```text
com.fiap.tech_challenge_fase2/
├── domain/            # Payment Entity, Enums, Value Objects
├── application/       # Use Cases, Ports (In/Out)
├── infrastructure/    # Mercado Pago Gateway, MySQL JPA Adapter, RabbitMQ Event Publisher
└── interfaces/        # PaymentController (REST API), DTOs, GlobalExceptionHandler
```

### Padrão Saga Coreografado (Choreographed Saga)

Este microsserviço atua como um **participante reativo** no Saga transacional:

1. **Geração de Cobrança**: Recebe solicitações de cobrança PIX via API REST (`POST /api/payments`).
2. **Processamento de Webhooks**: Recebe callbacks do Mercado Pago (`POST /api/payments/webhook`) com o status atualizado.
3. **Publicação de Eventos**:
   - `PaymentApprovedEvent` (Routing Key: `payment.approved`): Notifica a aprovação para avanço do fluxo da Ordem de Serviço.
   - `PaymentFailedEvent` (Routing Key: `payment.failed`): Aciona a compensação/rollback da Ordem de Serviço (`CANCELED`).

---

## 📑 Documentação da API (Swagger UI & Postman)

- **Swagger UI**: Disponível em `http://localhost:8081/swagger-ui.html` quando o serviço estiver em execução.
- **Coleção Postman**: Arquivo da coleção com o fluxo completo do Saga (Happy Path e Rollback):
  - 📄 **[postman_collection.json](./postman_collection.json)**

---

## 📊 Evidências de Cobertura de Testes

O projeto conta com suítes de testes unitários, testes de integração e cenários BDD com Cucumber. A verificação de cobertura é realizada via **JaCoCo** garantindo cobertura de instruções e branches superior a 80%.

![Evidência de Cobertura de Testes](./.docs/coverage.png)

---

> ℹ️ Para mais detalhes de implantação, pipelines CI/CD e execução local, consulte o repositório **[oficina-infra](https://github.com/CarlosDanyel/oficina-infra)**.
