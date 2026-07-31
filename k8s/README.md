# Kubernetes — payment-billing-service

Manifests para deploy do microsserviço de Pagamentos e Cobranças no Kubernetes.

---

## Estrutura

```
k8s/
├── 00-namespace.yaml       # Namespace fiap-oficina
├── 01-configmap.yaml       # ConfigMap (DB, RabbitMQ, Mercado Pago — não sensíveis)
├── 02-secret.yaml          # Secret (DB_PASSWORD, MERCADO_PAGO_ACCESS_TOKEN)
├── kustomization.yaml      # Kustomize (aplica todos os manifests)
└── app/
    ├── 04-deployment.yaml  # Deployment com 2 réplicas, probes na porta 8081
    ├── 05-service.yaml     # Service ClusterIP (porta 80 → 8081)
    └── 06-hpa.yaml         # HPA (CPU 70% / RAM 80%, min 2, max 10)
```

> **Infra compartilhada:** MySQL, RabbitMQ e MongoDB são gerenciados pelo repositório `oficina-infra`. O banco `payment_db` fica em instância MySQL isolada (`mysql-payment`), sem acesso aos dados do `ordem-de-service`.

---

## Pré-requisitos

```bash
minikube start --cpus=4 --memory=4096

# Metrics Server (necessário para HPA)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

---

## Deploy

```bash
kubectl apply -k k8s/

# Ou individualmente:
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml
kubectl apply -f k8s/app/04-deployment.yaml
kubectl apply -f k8s/app/05-service.yaml
kubectl apply -f k8s/app/06-hpa.yaml
```

---

## Configurar Secrets (ANTES de aplicar)

```bash
echo -n "oficina_pass" | base64          # DB_PASSWORD
echo -n "APP_USR-xxx" | base64           # MERCADO_PAGO_ACCESS_TOKEN
```

Edite o arquivo `02-secret.yaml` com os valores gerados.

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
