# oficina-os-service

Microsserviço responsável pelo ciclo de vida das **Ordens de Serviço (OS)** em uma oficina mecânica. Faz parte de uma arquitetura de microsserviços e se comunica com os serviços de Billing e Execution via eventos assíncronos (RabbitMQ).

## Tecnologias

- **Kotlin + Spring Boot 3.3** — Java 21
- **PostgreSQL 16** — dados relacionais da OS (Flyway para migrações)
- **MongoDB 6** — histórico de status e resumo da OS (timeline)
- **RabbitMQ 3.13** — mensageria assíncrona entre microsserviços
- **Springdoc OpenAPI** — documentação automática da API
- **Datadog APM** — rastreabilidade distribuída (agente Java embutido na imagem)
- **Docker + Docker Compose** — ambiente local completo

## Estrutura do projeto

```
src/main/kotlin/com/oficina_os_service/
├── application/          # Casos de uso (OrdemServicoService)
├── domain/
│   ├── enum/             # StatusOS, StatusItemServico
│   └── model/            # Modelos de domínio
└── infra/
    ├── client/           # Clientes HTTP (BillingService, ExecutionService)
    ├── config/           # OpenAPI config
    ├── controller/       # REST endpoints
    ├── dto/              # Request/Response DTOs
    ├── messaging/        # Consumers, Publisher, RabbitMQConfig, Events
    ├── nosql/            # Documentos e repositórios MongoDB
    └── repository/       # Entities e repositórios JPA (PostgreSQL)
```

## Fluxo de status da OS

```
RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → APROVADA → EM_EXECUCAO → FINALIZADA → ENTREGUE
                                                        └──────────────────────────────→ CANCELADA
```

## API REST

Base URL: `http://localhost:8080`

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/ordens` | Abre uma nova OS |
| `GET` | `/ordens/{id}` | Consulta OS com histórico e resumo |
| `GET` | `/ordens?clienteId={id}` | Lista todas as OS de um cliente |
| `PATCH` | `/ordens/{id}/status` | Atualiza status manualmente |
| `DELETE` | `/ordens/{id}?motivo=` | Cancela uma OS |

Documentação interativa disponível em `http://localhost:8080/swagger-ui.html` após subir o serviço.

### Exemplo — criar uma OS

```bash
curl -X POST http://localhost:8080/ordens \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "veiculoId": 42,
    "itensServico": [
      { "servicoId": 1, "quantidade": 1 }
    ],
    "itensEstoque": [
      { "estoqueId": 10, "quantidade": 2, "precoUnitario": 49.90 }
    ]
  }'
```

## Mensageria — RabbitMQ

### Eventos publicados por este serviço

| Routing key | Descrição |
|-------------|-----------|
| `os.criada` | Disparado ao abrir uma nova OS |
| `os.cancelada` | Disparado ao cancelar uma OS |

### Eventos consumidos por este serviço

| Routing key | Origem | Ação |
|-------------|--------|------|
| `orcamento.gerado` | Billing | Muda status para `AGUARDANDO_APROVACAO` |
| `orcamento.aprovado` | Billing | Muda status para `APROVADA` |
| `orcamento.rejeitado` | Billing | Cancela a OS |
| `pagamento.confirmado` | Billing | Confirma aprovação |
| `pagamento.falhou` | Billing | Cancela a OS |
| `execucao.iniciada` | Execution | Muda status para `EM_EXECUCAO` |
| `execucao.finalizada` | Execution | Muda status para `FINALIZADA` |

Todos os eventos trafegam pela exchange `os.exchange` (TopicExchange). Cada fila tem Dead Letter Queue configurada — após 3 tentativas de retry (2s → 4s → 8s), a mensagem vai para `<fila>.dlq`.


## Rodando localmente

### Pré-requisitos

- Docker e Docker Compose
- JDK 21 (para rodar sem Docker)

### Com Docker Compose (recomendado)

```bash
# 1. Configure as variáveis de ambiente
cp .env.example .env
# Edite .env se quiser apontar para CloudAMQP ou outro banco externo

# 2. Suba todos os serviços
docker compose --env-file .env up -d

# 3. Acompanhe os logs
docker compose logs -f os-service
```

Serviços disponíveis após o boot:

| Serviço | URL |
|---------|-----|
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| RabbitMQ Management | `http://localhost:15672` (guest/guest) |
| PostgreSQL | `localhost:5433` |
| MongoDB | `localhost:27017` |

### Sem Docker (apenas a aplicação)

Requer PostgreSQL, MongoDB e RabbitMQ rodando localmente nas portas padrão.

```bash
./gradlew bootRun
```

## Variáveis de ambiente

| Variável | Padrão (local) | Descrição |
|----------|---------------|-----------|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_NAME` | `oficina_os_db` | Nome do banco |
| `DB_USER` | `postgres` | Usuário |
| `DB_PASSWORD` | `12345os` | Senha |
| `MONGO_URI` | `mongodb://admin:...@localhost:27017/os_history` | URI completa do MongoDB local ou Atlas (`mongodb+srv://...`) |
| `MONGO_DATABASE` | `os_history` | Banco usado quando a URI não informa o nome |
| `RABBIT_HOST` | `localhost` | Host do RabbitMQ |
| `RABBIT_PORT` | `5672` | Porta (5671 para TLS/CloudAMQP) |
| `RABBIT_USER` | `guest` | Usuário |
| `RABBIT_PASS` | `guest` | Senha |
| `RABBIT_VHOST` | `/` | Virtual host |
| `RABBIT_SSL_ENABLED` | `false` | Habilita TLS (`true` no CloudAMQP) |
| `BILLING_SERVICE_URL` | `http://localhost:8081` | URL do Billing Service |
| `EXECUTION_SERVICE_URL` | `http://localhost:8082` | URL do Execution Service |

Nunca commite credenciais reais. Consulte [`docs/onde-colocar-credenciais.txt`](../docs/onde-colocar-credenciais.txt) para instruções por ambiente (local, GitHub Actions, Kubernetes).

## Banco de dados

As migrações são gerenciadas pelo **Flyway** e rodam automaticamente ao iniciar a aplicação.

| Migration | Descrição |
|-----------|-----------|
| `V1` | Tabelas `tb_ordem_servico`, `tb_item_servico`, `tb_item_estoque` |
| `V2` | Tabela `tb_servico` (catálogo de serviços) |
| `V3` | Campos `is_aprovado` e `data_aprovacao` na OS |

## Testes

```bash
# Rodar todos os testes
./gradlew test

# Rodar com relatório de cobertura (mínimo 80%)
./gradlew check

# Relatório HTML de cobertura
open build/reports/jacoco/test/html/index.html
```

## Health checks (Kubernetes)

O Actuator expõe as probes necessárias para Kubernetes:

| Probe | Endpoint |
|-------|----------|
| Liveness | `GET /actuator/health/liveness` |
| Readiness | `GET /actuator/health/readiness` |
| Métricas Prometheus | `GET /actuator/prometheus` |
