# Kubernetes — payment-billing-service

Manifests para deploy do microsserviço de Pagamentos e Cobranças no Kubernetes.

---

## Estrutura

```
k8s/
├── 00-namespace.yaml       # Namespace fiap-oficina
├── .env                    # Variáveis de ambiente e segredos (ConfigMap e Secret)
├── .env.example            # Modelo com todas as variáveis
├── kustomization.yaml      # Kustomize (gera ConfigMap/Secret a partir do .env e aplica tudo)
└── app/
    ├── 04-deployment.yaml  # Deployment com 2 réplicas, probes na porta 8081
    ├── 05-service.yaml     # Service ClusterIP (porta 80 → 8081)
    └── 06-hpa.yaml         # HPA (CPU 70% / RAM 80%, min 2, max 10)
```

> **Infra compartilhada:** MySQL, RabbitMQ e MongoDB são gerenciados pelo repositório `oficina-infra`. O banco `payment_db` fica em instância MySQL isolada (`mysql-payment`), sem acesso aos dados do `ordem-de-service`.

---

## Configuração (ANTES de aplicar)

Certifique-se de que o arquivo `.env` existe na pasta `k8s/`:

```bash
# Criar o .env a partir do exemplo se ainda não existir:
cp k8s/.env.example k8s/.env
```

---

## Deploy

```bash
# Aplicar todos os manifests via Kustomize
kubectl apply -k k8s/
```

---

## Verificar o deploy

```bash
kubectl get pods -n fiap-oficina -w
kubectl logs -f deployment/payment-billing-service -n fiap-oficina
kubectl describe hpa payment-billing-hpa -n fiap-oficina
```

---

## Acessar localmente

```bash
kubectl port-forward svc/payment-billing-service 8081:80 -n fiap-oficina
# Swagger UI: http://localhost:8081/swagger-ui.html
# API:        http://localhost:8081/api/payments
```

---

## Testar HPA

```bash
kubectl get hpa payment-billing-hpa -n fiap-oficina -w

kubectl run load-test --image=busybox --restart=Never -n fiap-oficina \
  -- /bin/sh -c "while true; do wget -q -O- http://payment-billing-service/api/payments/service-order/SO-001; done"

kubectl delete pod load-test -n fiap-oficina
```

---

## Escalonamento automático

```
Carga normal → 2 réplicas (mínimo)
Carga alta (CPU > 70% ou RAM > 80%) → até 10 réplicas (máximo)
  └─ adiciona 2 pods por vez, a cada 60s
Carga cai (> 5 min abaixo dos thresholds) → reduz 1 pod a cada 2 min
```

---

## Remover tudo

```bash
kubectl delete -k k8s/
```
