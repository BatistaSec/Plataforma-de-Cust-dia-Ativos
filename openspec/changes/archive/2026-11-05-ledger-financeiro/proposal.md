# Proposal

## Por que

A change anterior (`integridade-financeira-transferencias`) implementou bloqueio de saldo, lock otimista, idempotência básica e histórico imutável via `asset_movements`. No entanto, o modelo atual é de entrada única — cada operação gera um único registro `AssetMovement` sem agrupamento transacional.

Para uma plataforma de custódia, é necessário evoluir para um **ledger contábil estruturado** com:

- **Partidas dobradas**: cada operação pode gerar múltiplas entradas (débito + crédito) agrupadas em uma transação, garantindo balanceamento por ativo.
- **Rastreabilidade completa**: capacidade de reconstruir a posição de qualquer portfólio a partir dos lançamentos do ledger, sem depender do saldo materializado em `portfolio_assets`.
- **Idempotência robusta**: `Idempotency-Key` como HTTP header (não no body), com hash dos parâmetros da requisição, rejeitando reuso da chave com conteúdo diferente.
- **Precisão por ativo**: escala e regras de arredondamento configuráveis por tipo de ativo (nem todos os ativos precisam de 5 casas decimais).
- **Consulta de extrato**: endpoints para extrato e histórico de lançamentos por portfólio e ativo.

## O que Muda

### 2.1 — Ledger Financeiro

- **`LedgerTransaction`** (nova entidade): agrupa lançamentos de uma operação. Campos: `id`, `reference` (única — ex: orderId), `transactionType` (ORDER, TRANSFER, ADJUSTMENT), `status` (PENDING, CONFIRMED, REJECTED), `createdAt`.
- **`LedgerEntry`** (nova entidade, substitui `AssetMovement`): cada débito ou crédito individual. Campos: `id`, `transactionId` (FK → LedgerTransaction), `accountId` (= portfolioId), `assetId`, `entryType` (DEBIT, CREDIT), `quantity` (BigDecimal), `createdAt`. Todos os campos `@Column(updatable = false)`.
- **Balanceamento por ativo**: para transações do tipo TRANSFER, a soma de débitos deve igualar a soma de créditos por ativo.
- **Reconstrução de posição**: o saldo de um portfólio para um ativo pode ser recalculado somando CREDIT e subtraindo DEBIT das `LedgerEntry`.
- **Imutabilidade confirmada**: lançamentos com `status = CONFIRMED` não podem ser alterados ou excluídos.
- **Endpoint de extrato**: `GET /api/v1/custody/ledger/statement?portfolioId=X&assetId=Y` retorna lançamentos paginados ordenados por `createdAt DESC`.

### 2.2 — Precisão Monetária e Quantitativa

- **Escala por ativo**: campo `scale` (Integer) na entidade `Asset`/`AssetEntity` define o número de casas decimais permitidas. Default: 5.
- **Validação de escala**: o use case valida se a escala do `BigDecimal` informado não excede a escala configurada do ativo.
- **Rejeição de NaN/Infinity**: validação na fronteira da API (request) rejeita valores não-finitos.
- **RoundingMode**: `HalfEven` (banker's rounding) como padrão para operações que precisam arredondar.
- **Mantém BigDecimal**: sem `double` ou `float` — regra já implementada, esta change reforça e adiciona validação por ativo.

### 2.3 — Atomicidade e Concorrência

- **Mantém `@Transactional`** no use case (já implementado).
- **Mantém `@Version`** (lock otimista) em `PortfolioAssetEntity` (já implementado).
- **Avalia `PESSIMISTIC_WRITE`** para operações de transferência entre portfólios (alta contenção esperada sobre o mesmo ativo). Documenta quando usar cada estratégia.
- **Tratamento de deadlock**: `CannotAcquireLockException` e `PessimisticLockingFailureException` mapeados para HTTP 409 com mensagem orientando retry.
- **Política de isolamento**: `READ_COMMITTED` (default do PostgreSQL) — documentada e justificada.

### 2.4 — Idempotência Robusta

- **`Idempotency-Key` como HTTP header**: em vez de campo no body. O controller extrai o header e repassa ao use case.
- **Hash dos parâmetros**: armazena `SHA-256` do body da requisição junto à chave. Se a mesma chave for reusada com body diferente, rejeita com HTTP 409.
- **Tabela `idempotency_records`**: nova tabela com `idempotencyKey` (UUID, UNIQUE), `requestHash` (VARCHAR), `responseSnapshot` (JSONB), `createdAt`. Substitui o `idempotencyKey` direto na `custody_orders`.
- **Rejeição de reuso com conteúdo diferente**: se a chave existe mas o hash difere, retorna 409 com mensagem "Idempotency-Key já utilizado com parâmetros diferentes".
- **Retorno do resultado original**: se a chave existe e o hash coincide, retorna o `responseSnapshot` armazenado com status 200.
- **Concorrência**: unique constraint em `idempotencyKey` garante que apenas uma das requisições simultâneas consiga inserir; a outra recebe `DataIntegrityViolationException` e consulta o registro existente.

## Fora do Escopo

- Migrar dados históricos de `asset_movements` para `ledger_entries` — fica como tarefa operacional futura.
- Transferência entre portfólios (endpoint dedicado) — esta change prepara a estrutura, mas o endpoint de transferência é change futura.
- Cash balance / saldo de caixa — o sistema opera com posições de ativos.
- Deploy, infraestrutura AWS, Terraform.
- Alterações em `event-processor`, `audit-service`, `auth-service` ou `api-gateway`.
- Flyway/Liquibase — `ddl-auto: update` cria as novas tabelas.

## Capacidades

### Novas Capacidades

- `custody/ledger-financeiro`: o `custody-service` mantém um ledger contábil estruturado com transações agrupando lançamentos de débito e crédito, balanceamento por ativo, idempotência robusta via HTTP header com hash de parâmetros, precisão monetária configurável por ativo e consultas de extrato.

### Capacidades Modificadas

- `custody/integridade-financeira`: a criação de ordens passa a registrar no ledger (`LedgerTransaction` + `LedgerEntry`) em vez de `AssetMovement` direto. O `AssetMovement` mantido por compatibilidade, mas novos registros usam o ledger.

## Impacto

**Microsserviço afetado:** `custody-service` exclusivamente.

**Camadas afetadas:**

| Camada | Arquivos | Ação |
|---|---|---|
| `domain` | `LedgerTransaction.java` | CRIAR — modelo de domínio |
| `domain` | `LedgerEntry.java` | CRIAR — modelo de domínio |
| `domain` | `enums/TransactionType.java` | CRIAR — ORDER, TRANSFER, ADJUSTMENT |
| `domain` | `enums/TransactionStatus.java` | CRIAR — PENDING, CONFIRMED, REJECTED |
| `domain` | `enums/EntryType.java` | CRIAR — DEBIT, CREDIT (ou reusar MovementType) |
| `domain` | `repositories/LedgerTransactionRepository.java` | CRIAR |
| `domain` | `repositories/LedgerEntryRepository.java` | CRIAR |
| `domain` | `repositories/IdempotencyRecordRepository.java` | CRIAR |
| `domain` | `models/IdempotencyRecord.java` | CRIAR |
| `domain` | `models/Asset.java` | MODIFICAR — adicionar campo `scale` |
| `domain` | `exceptions/IdempotencyKeyConflictException.java` | CRIAR |
| `application` | `CreateOrderUseCase.java` | MODIFICAR — usar ledger + idempotency record |
| `application` | `dto/CreateOrderInput.java` | MODIFICAR — idempotencyKey sai do body, vem do header |
| `application` | `GetLedgerStatementUseCase.java` | CRIAR — consulta de extrato |
| `infrastructure` | `LedgerTransactionEntity.java` | CRIAR |
| `infrastructure` | `LedgerEntryEntity.java` | CRIAR |
| `infrastructure` | `IdempotencyRecordEntity.java` | CRIAR |
| `infrastructure` | `AssetEntity.java` | MODIFICAR — adicionar `scale` |
| `infrastructure` | `SpringData*Repository.java` | CRIAR — 3 novos |
| `infrastructure` | `*Mapper.java` | CRIAR — 3 novos |
| `infrastructure` | `*RepositoryImpl.java` | CRIAR — 3 novos |
| `presentation` | `CustodyController.java` | MODIFICAR — extrair Idempotency-Key do header |
| `presentation` | `LedgerStatementResponse.java` | CRIAR |
| `presentation` | `GlobalExceptionHandler.java` | MODIFICAR — IdempotencyKeyConflictException → 409, PessimisticLockingFailureException → 409 |
| `presentation` | `CustodyOrderRequest.java` | MODIFICAR — remover idempotencyKey do body |

**Sem impacto em:** Kafka (`OrderEvent` não muda), Redis, CI/CD, docker-compose, Terraform.

## Decisões Pendentes

- **D-1 (LedgerEntry substitui AssetMovement?):** Recomendação: coexistir durante esta change, deprecariar `AssetMovement` em change futura. O `CreateOrderUseCase` grava em ambos durante a transição.
- **D-2 (Partidas dobradas para BUY/SELL simples):** BUY gera 1 CREDIT, SELL gera 1 DEBIT — sem contrapartida. O balanceamento débito=crédito só se aplica a TRANSFER (2+ entradas). Alternativa: introduzir "conta de custódia" como contrapartida — mais complexa, fica para avaliação futura.
- **D-3 (PESSIMISTIC_WRITE vs @Version):** Manter @Version para operações de portfólio individuais. Avaliar PESSIMISTIC_WRITE apenas para transferências entre portfólios (alta contenção sobre o mesmo ativo).
- **D-4 (IdempotencyRecord separado ou na CustodyOrderEntity):** Recomendação: tabela separada `idempotency_records` com `responseSnapshot` — permite reusar idempotência para qualquer endpoint, não apenas orders.
- **D-5 (Escala default do ativo):** Recomendação: 5 casas decimais (compatível com o `precision=38, scale=5` já configurado). Alguns ativos podem ter scale=0 (ações inteiras).
