# Kubernetes — OS Service

## Estrutura

```
k8s/
├── base/
│   ├── deployment.yaml   # Deployment (usa IMAGE_PLACEHOLDER e NAMESPACE_PLACEHOLDER)
│   └── service.yaml      # ClusterIP porta 80 → 8080
├── develop/
│   └── configmap.yaml    # ConfigMap do ambiente de desenvolvimento
└── producao/
    └── configmap.yaml    # ConfigMap do ambiente de produção
```

## Deploy manual

```bash
NAMESPACE=oficina-mvp-prod
IMAGE=<account>.dkr.ecr.us-east-1.amazonaws.com/oficina-os-service:<sha>

kubectl create secret generic os-service-secrets \
  --namespace=$NAMESPACE \
  --from-literal=DB_PASSWORD=<senha-postgres> \
  --from-literal=MONGO_URI=<uri-completa-mongodb-atlas> \
  --from-literal=RABBIT_HOST=<host-cloudamqp> \
  --from-literal=RABBIT_USER=<usuario-cloudamqp> \
  --from-literal=RABBIT_PASS=<senha-cloudamqp> \
  --from-literal=RABBIT_VHOST=<vhost-cloudamqp>

kubectl apply -f k8s/producao/configmap.yaml

sed "s|NAMESPACE_PLACEHOLDER|$NAMESPACE|g" k8s/base/service.yaml \
  | kubectl apply -f -

sed "s|IMAGE_PLACEHOLDER|$IMAGE|g; s|NAMESPACE_PLACEHOLDER|$NAMESPACE|g" \
  k8s/base/deployment.yaml | kubectl apply -f -

kubectl rollout status deployment/os-service -n $NAMESPACE
```

## Probes

| Probe | Endpoint | Delay |
|-------|----------|-------|
| Liveness | `GET /actuator/health/liveness` | 90s |
| Readiness | `GET /actuator/health/readiness` | 60s |

## Secrets necessários (GitHub Actions)

| Secret | Descrição |
|--------|-----------|
| `AWS_ACCESS_KEY_ID` | Credencial AWS |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS |
| `AWS_SESSION_TOKEN` | Token de sessão (se temporário) |
| `SONAR_TOKEN` | Token do SonarCloud |
| `SONAR_PROJECT_KEY` | Chave do projeto no SonarCloud |
| `SONAR_ORGANIZATION` | Organização no SonarCloud |
| `OS_DB_PASSWORD` | Senha do PostgreSQL exclusivo do OS Service |
| `MONGO_URI` | URI completa e secreta do MongoDB Atlas |
| `RABBIT_HOST` | Host do CloudAMQP, sem protocolo |
| `RABBIT_USER` | Usuário do CloudAMQP |
| `RABBIT_PASS` | Senha do CloudAMQP |
| `RABBIT_VHOST` | Virtual host do CloudAMQP |
