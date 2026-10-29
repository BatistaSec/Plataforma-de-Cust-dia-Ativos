# Proposal

## Why

Os endpoints `GET /api/v1/audit/portfolio/{id}` e `GET /api/v1/audit/order/{id}` do `audit-service` retornam `List<AuditLogResponse>` — um array JSON irrestrito sem paginação. Em produção, portfólios ativos acumulam centenas de eventos por dia; uma lista completa força o banco a materializar todos os registros em memória e sobrecarrega o cliente, tornando os endpoints inviáveis em volume.

## What Changes

- **BREAKING** — Os dois endpoints passam a retornar um envelope paginado (`PagedAuditResponse`) em vez de array JSON. Clientes que deserializam como `List<AuditLogResponse>` precisam ser migrados antes do deploy.
- Adicionados query parameters opcionais: `page` (default `0`), `size` (default `20`, máx. `100`), `eventType` (filtro por `AuditEventType`), `from` e `to` (janela temporal ISO-8601, inclusivos).
- Validação de parâmetros com respostas HTTP 400 descritivas.
- Novos índices compostos em `audit_logs` (`portfolio_id, occurred_at DESC` e `order_id, occurred_at DESC`) para suportar a query com ordenação eficiente.
- Novos use cases: `GetPagedAuditLogsByPortfolioUseCase` e `GetPagedAuditLogsByOrderUseCase`.
- Novo record de domínio `Page<T>` (sem dependência de Spring Data).
- Nova classe de critério de busca `AuditLogFilter` (em `domain.query` — não em `application.dto`).
- Quatro queries JPQL especializadas em `SpringDataAuditLogRepository` (com/sem `eventType`, separadas para evitar bug do Hibernate 6 com enums nullable).
- `GlobalExceptionHandler` enriquecido com handler para `MethodArgumentTypeMismatchException` (enums inválidos) e handler 500 sem vazamento de `ex.getMessage()`.

## Capabilities

### New Capabilities

- `audit/consulta-paginada`: Capacidade do `audit-service` de retornar histórico de auditoria de forma paginada, com filtros opcionais por tipo de evento e janela temporal, ordenado por `occurredAt DESC`.

### Modified Capabilities

*(Nenhuma capability existente tem spec-level behavior alterado — o comportamento de gravação de eventos permanece idêntico.)*

## Impact

**Microsserviço afetado:** `audit-service` exclusivamente.

**Camadas afetadas:**
- `domain` — novos: `Page<T>`, `FilterSubject`, `AuditLogFilter`; modificado: `AuditLogRepository` (novo método `findPaged`)
- `application` — novos: `GetPagedAuditLogsByPortfolioUseCase`, `GetPagedAuditLogsByOrderUseCase`
- `infrastructure` — modificados: `SpringDataAuditLogRepository` (4 queries JPQL), `AuditLogRepositoryImpl` (implementa `findPaged`), `AuditLogEntity` (índices compostos)
- `presentation` — modificados: `AuditController` (novos query params, tipo de retorno), `GlobalExceptionHandler` (novos handlers)

**Use cases legados preservados:** `GetAuditLogsByOrderUseCase` e `GetAuditLogsByPortfolioUseCase` continuam no código sem modificação (dead code candidato a remoção futura).

**Sem impacto em:** `api-gateway`, `auth-service`, `custody-service`, `event-processor`, Kafka, Redis, Terraform, CI/CD, docker-compose.

**Pendências que requerem decisão antes do deploy:**
- **P-1** — Critério de desempate: adicionar `id ASC` como secondary sort para garantir paginação determinística quando dois registros têm o mesmo `occurredAt`.
- **P-2** — Migração de clientes: inventariar consumidores dos endpoints v1 antes do deploy (breaking change de formato).
- **P-5** — Timezone: `occurredAt` é `LocalDateTime` sem timezone; formato da API pode ser ambíguo para clientes em fusos diferentes.
