# language: pt
Funcionalidade: Fluxo de Pagamento via Saga Coreografado

  Contexto:
    Dado que o sistema de pagamento está operacional

  Cenário: Criar pagamento PIX para uma Ordem de Serviço
    Quando eu solicito a criação de um pagamento para a OS "SO-001" no valor de R$ 150.00
    Então o pagamento deve ser criado com status "PENDING"
    E o método de pagamento deve ser "PIX"
    E o ID da OS no pagamento deve ser "SO-001"

  Cenário: Buscar pagamento existente por Ordem de Serviço
    Dado que existe um pagamento para a OS "SO-002"
    Quando eu consulto o pagamento da OS "SO-002"
    Então o pagamento deve ser retornado com sucesso

  Cenário: Webhook aprova pagamento e dispara evento Saga
    Dado que existe um pagamento PENDING para a OS "SO-003"
    Quando o webhook do Mercado Pago notifica aprovação do pagamento "EXT-APPROVED"
    Então o status do pagamento deve ser atualizado para "APPROVED"
    E um evento PaymentApprovedEvent deve ser publicado no RabbitMQ

  Cenário: Webhook rejeita pagamento e dispara rollback do Saga
    Dado que existe um pagamento PENDING para a OS "SO-004"
    Quando o webhook do Mercado Pago notifica rejeição do pagamento "EXT-REJECTED"
    Então o status do pagamento deve ser atualizado para "REJECTED"
    E um evento PaymentFailedEvent deve ser publicado no RabbitMQ

  Cenário: Pagamento não encontrado deve retornar erro
    Quando eu consulto o pagamento da OS "SO-NOT-FOUND"
    Então o sistema deve retornar status 404
