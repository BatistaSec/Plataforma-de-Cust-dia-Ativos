# Plataforma de Custódia de Ativos

Plataforma de custódia de ativos construída com **Java 21, Spring Boot, Clean Architecture e Arquitetura Orientada a Eventos (EDA)**.

---

## 🚀 Stack

| Camada | Tecnologias |
|---|---|
| **Backend** | Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA |
| **Mensageria** | Apache Kafka (MSK na AWS) |
| **Banco de dados** | PostgreSQL 16 (RDS na AWS) |
| **Cache** | Redis 7 (ElastiCache na AWS) |
| **Arquitetura** | Clean Architecture, DDD, SOLID, Event-Driven (EDA) |
| **Observabilidade** | Actuator, Prometheus, Grafana, OpenTelemetry, AWS CloudWatch |
| **Infraestrutura** | Docker, Terraform, AWS ECS Fargate, ECR, ALB, VPC |
| **CI/CD** | GitHub Actions (Build → Test → Security → Docker → Terraform → Deploy → Smoke Tests) |

---

## 🏗️ Arquitetura de Microsserviços

```
                        Internet
                            │
                    ┌───────▼────────┐
                    │      ALB       │
                    └───────┬────────┘
                            │
                    ┌───────▼────────┐
                    │  api-gateway   │  :8080
                    │  (JWT filter)  │
                    └───┬───────┬────┘
                        │       │
           ┌────────────▼─┐   ┌─▼──────────────┐
           │ auth-service │   │ custody-service │
           │    :8081     │   │     :8082       │
           └──────────────┘   └────────┬────────┘
                                       │ publica
                                       ▼
                              [custody-orders-topic]
                               ┌────────┴────────┐
                               │                 │
                  ┌────────────▼──┐   ┌──────────▼────┐
                  │event-processor│   │ audit-service  │
                  │    :8083      │   │    :8084       │
                  └───────┬───────┘   └───────────────┘
                          │ publica
                          ▼
                 [processed-orders-topic]
                          │
                 ┌────────▼────────┐
                 │  audit-service  │
                 │  (ORDER_PROCESSED)
                 └─────────────────┘
```

---

## 📦 Microsserviços

### 🚪 API Gateway — porta `8080`
Ponto único de entrada. Valida JWT em todas as requisições e roteia para os serviços internos via Spring Cloud Gateway.

### 🛡️ Auth Service — porta `8081`
Autenticação e registro de usuários. Gera tokens JWT com o `userId` como claim. Endpoints públicos.

**Response do login/register:**
```json
{ "token": "eyJhbGci...", "userId": "uuid-do-usuario" }
```

### 💼 Custody Service — porta `8082`
Core do negócio. Gerencia portfólios, ativos e ordens. Usa Redis para cache e publica `OrderEvent` no Kafka ao criar uma ordem.

### ⚙️ Event Processor — porta `8083`
Worker assíncrono. Consome `custody-orders-topic`, aplica idempotência, processa a ordem, salva no banco e publica `ProcessedOrderEvent` no Kafka. Possui retry automático com DLT.

### 🔍 Audit Service — porta `8084`
Auditoria de negócio. Consome os dois tópicos Kafka e registra **todos os eventos** na tabela `audit_logs`. Permite rastrear o histórico completo de qualquer ordem ou portfólio.

---

## 🔌 Endpoints

> Todos os endpoints exceto `/api/v1/auth/**` exigem `Authorization: Bearer <token>`.
> Todos os parâmetros são passados via `@RequestBody` (JSON).

### 🛡️ Auth Service
| Método | Endpoint | Body |
|---|---|---|
| `POST` | `/api/v1/auth/register` | `{ "email": "", "password": "" }` |
| `POST` | `/api/v1/auth/login` | `{ "email": "", "password": "" }` |

### 💼 Custody Service
| Método | Endpoint | Body |
|---|---|---|
| `POST` | `/api/v1/custody/portfolios` | `{ "userId": "uuid" }` |
| `POST` | `/api/v1/custody/portfolios/user` | `{ "userId": "uuid" }` |
| `POST` | `/api/v1/custody/assets` | `{ "ticker": "PETR4", "name": "Petrobras" }` |
| `GET`  | `/api/v1/custody/assets` | — |
| `POST` | `/api/v1/custody/orders` | `{ "portfolioId": "uuid", "assetId": "uuid", "type": "BUY", "quantity": 10.5 }` |

### ⚙️ Event Processor
| Método | Endpoint | Body |
|---|---|---|
| `GET`  | `/api/v1/processed-orders/{orderId}` | — |
| `POST` | `/api/v1/processed-orders/reprocess` | `{ "orderId": "uuid" }` |

### 🔍 Audit Service
| Método | Endpoint | Body |
|---|---|---|
| `GET` | `/api/v1/audit/order/{orderId}` | — |
| `GET` | `/api/v1/audit/portfolio/{portfolioId}` | — |

---

## 🛠️ Como rodar localmente

### 1. Subir infraestrutura base
```bash
docker-compose up -d
```
Sobe PostgreSQL (porta `15432`), Redis (`6379`) e Kafka (`9092`).

### 2. Subir observabilidade (opcional)
```bash
docker-compose -f docker-compose.observability.yml up -d
```
Sobe Prometheus (`9090`), Grafana (`3000`, admin/admin) e OpenTelemetry Collector (`4318`).

### 3. Iniciar os serviços
Cada serviço pode ser iniciado pela IDE ou via terminal dentro do diretório:
```bash
cd auth-service && ./mvnw spring-boot:run
cd custody-service && ./mvnw spring-boot:run
cd event-processor && ./mvnw spring-boot:run
cd audit-service && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
```

### 4. Testar com Insomnia
Importe o arquivo `insomnia-collection.json` na raiz do projeto. A coleção já vem com todas as rotas e variáveis de ambiente configuradas.

**Fluxo sugerido:**
1. Register → pega `token` e `userId`
2. Preenche as variáveis no environment do Insomnia
3. Criar ativo → pega `assetId`
4. Criar portfólio → pega `portfolioId`
5. Criar ordem → pega `orderId`
6. Consultar ordem processada e auditoria

---

## ☁️ Deploy na AWS (Terraform)

A infraestrutura completa está em `terraform/` e provisiona:

- **VPC** com subnets públicas e privadas, NAT Gateway
- **RDS** PostgreSQL 16 criptografado
- **MSK** Kafka com 2 brokers
- **ElastiCache** Redis
- **ECR** com 1 repositório por serviço
- **ECS Fargate** cluster com os 5 serviços
- **ALB** com target group por serviço

```bash
cd terraform
make bootstrap   # cria bucket S3 + DynamoDB para estado remoto (uma vez)
make init
make plan
make apply
```

---

## 🔄 CI/CD (GitHub Actions)

Cada serviço tem seu próprio workflow em `.github/workflows/`. O pipeline completo:

  

Workflows disponíveis:
- `ci-cd-api-gateway.yml`
- `ci-cd-auth-service.yml`
- `ci-cd-custody-service.yml`
- `ci-cd-event-processor.yml`
- `ci-cd-audit-service.yml`

Secrets necessários documentados em `.github/SECRETS.md`.

---

## 📊 Observabilidade

| Ferramenta | URL local | Finalidade |
|---|---|---|
| **Actuator** | `/actuator/health` em cada serviço | Health check e métricas |
| **Prometheus** | `http://localhost:9090` | Coleta métricas de todos os serviços |
| **Grafana** | `http://localhost:3000` (admin/admin) | Dashboards visuais |
| **OTel Collector** | `http://localhost:4318` | Recebe traces distribuídos |
| **CloudWatch** | AWS Console | Logs e métricas em produção |

No Grafana, importe o dashboard **Spring Boot Statistics** (ID `6756`).

---

## 📁 Estrutura do Repositório

```
projeto-portifolio/
├── api-gateway/               ← Spring Cloud Gateway + filtro JWT
├── auth-service/              ← Autenticação + JWT
├── custody-service/           ← Core de negócio
├── event-processor/           ← Worker Kafka assíncrono
├── audit-service/             ← Auditoria de negócio
├── terraform/                 ← Infraestrutura AWS como código
│   ├── modules/               ← VPC, RDS, MSK, ECS, ECR, ALB...
│   └── infrastructure.tf      ← Orquestração de todos os módulos
├── observability/             ← Configs Prometheus, Grafana, OTel
├── .github/workflows/         ← Pipelines CI/CD por serviço
├── docker-compose.yml         ← Infraestrutura local (Postgres, Redis, Kafka)
├── docker-compose.observability.yml ← Stack de observabilidade local
└── insomnia-collection.json   ← Coleção de endpoints para testes
```

---

## 🎯 Fases de Desenvolvimento

- [x] Fase 1 — Infraestrutura local (Docker Compose: PostgreSQL, Redis, Kafka)
- [x] Fase 2 — Auth Service (JWT, Spring Security, Clean Architecture)
- [x] Fase 3 — API Gateway (roteamento, filtro JWT)
- [x] Fase 4 — Custody Service (DDD, Redis cache, Kafka producer)
- [x] Fase 5 — Event Processor (Kafka consumer, idempotência, retry + DLT)
- [x] Fase 6 — Audit Service (auditoria de negócio via Kafka)
- [x] Fase 7 — Observabilidade (Actuator, Prometheus, Grafana, OTel, CloudWatch)
- [x] Fase 8 — CI/CD (GitHub Actions, Docker, ECR, ECS, Terraform, Smoke Tests)
- [x] Fase 9 — Infraestrutura AWS (Terraform: VPC, RDS, MSK, ECS Fargate, ALB)
