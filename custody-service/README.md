# Custody Service

Microsserviço responsável por gerenciar portfólios, ativos e ordens de custódia.

---

## Tecnologias

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Maven

---

## Como executar

```bash
./mvnw spring-boot:run
```

O serviço sobe na porta **8082**.

---

## Endpoints

Base URL: `http://localhost:8082/api/v1/custody`

> Todos os endpoints que recebem dados utilizam `@RequestBody` (JSON no corpo da requisição).

---

### Portfólios

#### Criar portfólio

```
POST /portfolios
```

**Body:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response `200`:**
```json
{
  "id": "uuid-do-portfolio",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "assets": []
}
```

---

#### Buscar portfólio por usuário

```
POST /portfolios/user
```

**Body:**
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response `200`:**
```json
{
  "id": "uuid-do-portfolio",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "assets": [
    {
      "id": "uuid-do-portfolio-asset",
      "assetId": "uuid-do-ativo",
      "ticker": "PETR4",
      "assetName": "Petrobras",
      "quantity": 10.0
    }
  ]
}
```

---

### Ativos

#### Criar ativo

```
POST /assets
```

**Body:**
```json
{
  "ticker": "PETR4",
  "name": "Petrobras"
}
```

**Response `200`:**
```json
{
  "id": "uuid-do-ativo",
  "ticker": "PETR4",
  "name": "Petrobras"
}
```

---

#### Listar todos os ativos

```
GET /assets
```

**Response `200`:**
```json
[
  {
    "id": "uuid-do-ativo",
    "ticker": "PETR4",
    "name": "Petrobras"
  }
]
```

---

### Ordens

#### Criar ordem de compra/venda

```
POST /orders
```

**Body:**
```json
{
  "portfolioId": "uuid-do-portfolio",
  "assetId": "uuid-do-ativo",
  "type": "BUY",
  "quantity": 10.5
}
```

> `type` aceita: `BUY` ou `SELL`

**Response `200`:**
```json
{
  "id": "uuid-da-ordem",
  "portfolioId": "uuid-do-portfolio",
  "assetId": "uuid-do-ativo",
  "type": "BUY",
  "quantity": 10.5,
  "status": "PENDING",
  "createdAt": "2026-10-29T10:00:00"
}
```

---

## Estrutura do projeto

```
custody-service/
├── src/main/java/com/custody/custody_service/
│   ├── application/
│   │   ├── dto/                  # Inputs e Outputs dos use cases
│   │   └── usecases/             # Regras de negócio (CreatePortfolio, GetPortfolio, ...)
│   ├── domain/
│   │   ├── enums/                # OrderType (BUY, SELL)
│   │   ├── exceptions/           # ResourceNotFoundException
│   │   ├── models/               # Portfolio, PortfolioAsset, Asset, Order
│   │   └── repositories/         # Interfaces de repositório (domínio)
│   ├── infrastructure/
│   │   ├── mappers/              # Conversão entre domain model e JPA entity
│   │   ├── persistence/
│   │   │   ├── adapters/         # Implementações dos repositórios
│   │   │   ├── entities/         # Entidades JPA
│   │   │   └── repositories/     # Spring Data JPA interfaces
│   │   └── messaging/            # Publicação de eventos (Kafka)
│   └── presentation/
│       ├── controllers/          # CustodyController
│       ├── request/              # DTOs de entrada (RequestBody)
│       └── response/             # DTOs de saída
└── src/main/resources/
    └── application.yml
```
