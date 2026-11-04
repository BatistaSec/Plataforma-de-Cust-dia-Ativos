# Proposal

## Por que

O `custody-service` aceita ordens de compra e venda de ativos sem qualquer verificação de saldo, controle de concorrência ou proteção contra duplicidade. Hoje é possível:

- Enviar uma ordem SELL sem que o portfólio tenha a quantidade necessária do ativo.
- Disparar duas requisições idênticas simultaneamente e criar dois registros de ordem.
- Atualizar `PortfolioAsset.quantity` em duas threads ao mesmo tempo sem conflito detectado (sem `@Version` nem lock pessimista), resultando em perdas ou ganhos silenciosos de posição.
- Não há registro imutável de débitos e créditos — qualquer edição no banco pode alterar retroativamente a posição de um portfólio.

Em uma plataforma de custódia financeira esses são riscos críticos de integridade de dados.

## O que Muda

- **Bloqueio de saldo insuficiente:** ordens SELL são rejeitadas com HTTP 422 se a posição em `portfolio_assets` for inferior à quantidade solicitada.
- **Controle de concorrência otimista:** `PortfolioAssetEntity` recebe `@Version Long version`; conflitos simultâneos lançam exceção detectável.
- **Idempotência de ordens:** `CustodyOrderEntity` recebe campo `idempotencyKey` (UUID, unique constraint). Requisições repetidas com o mesmo `idempotencyKey` retornam a ordem já criada sem efeito colateral.
- **Transferência atômica de posição:** o débito / crédito de `PortfolioAsset.quantity` e a criação da ordem ocorrem em um único `@Transactional`, nunca parcialmente.
- **Histórico imutável de movimentações:** nova tabela `asset_movements` — cada alteração de posição gera uma entrada com campos imutáveis (sem setters pós-construção): `id`, `portfolioId`, `assetId`, `type` (DEBIT/CREDIT), `quantity`, `referenceOrderId`, `occurredAt`.
- **Precisão monetária:** `BigDecimal` já é usado para `quantity` em todos os modelos existentes; esta change mantém e reforça esse padrão, sem nenhum `double` ou `float`.

## Fora do Escopo

- Saldo de caixa (Account, cash balance) — o sistema opera com posições de ativos, não com dinheiro em conta.
- Precificação de ativos (price, notional value) — a order não carrega preço unitário.
- Migração via Flyway/Liquibase — o projeto usa `ddl-auto: update`; novas colunas e tabelas serão criadas automaticamente no próximo start.
- Alterações em `event-processor`, `audit-service`, `auth-service` ou `api-gateway`.
- Endpoints de consulta do histórico de movimentações (escopo de uma change futura).
- Deploy, infraestrutura AWS, Terraform.

## Capacidades

### Novas Capacidades

- `custody/integridade-financeira`: O `custody-service` garante que ordens só sejam aceitas quando há posição suficiente (SELL), que operações concorrentes sobre a mesma posição sejam detectadas e rejeitadas de forma segura, que requisições duplicadas sejam idempotentes e que cada mutação de posição produza um registro imutável auditável.

### Capacidades Modificadas

*(Nenhuma capability existente tem spec-level behavior alterado — `CreateOrderUseCase` é enriquecido mas mantém a mesma interface HTTP.)*

## Impacto

**Microsserviço afetado:** `custody-service` exclusivamente.

**Camadas afetadas:**

| Camada | Arquivos | Ação |
|---|---|---|
| `domain` | `PortfolioAsset.java` | Adicionar validação `debit(quantity)` |
| `domain` | `CustodyOrder.java` | Adicionar campo `idempotencyKey` |
| `domain` | `AssetMovement.java` | CRIAR — entidade de domínio imutável |
| `domain` | `domain/enums/MovementType.java` | CRIAR — enum DEBIT / CREDIT |
| `domain` | `domain/repositories/AssetMovementRepository.java` | CRIAR — interface |
| `application` | `CreateOrderUseCase.java` | MODIFICAR — verificação de saldo, idempotência, criação de AssetMovement |
| `application` | `application/dto/CreateOrderInput.java` | MODIFICAR — adicionar campo `idempotencyKey` |
| `application` | `application/dto/OrderOutput.java` | MODIFICAR — expor `idempotencyKey` |
| `infrastructure` | `PortfolioAssetEntity.java` | MODIFICAR — adicionar `@Version` |
| `infrastructure` | `CustodyOrderEntity.java` | MODIFICAR — adicionar `idempotencyKey` com `@Column(unique=true)` |
| `infrastructure` | `AssetMovementEntity.java` | CRIAR — entidade JPA imutável |
| `infrastructure` | `SpringDataAssetMovementRepository.java` | CRIAR |
| `infrastructure` | `AssetMovementRepositoryImpl.java` | CRIAR |
| `infrastructure` | `AssetMovementMapper.java` | CRIAR |
| `presentation` | `CustodyOrderRequest.java` | MODIFICAR — adicionar campo `idempotencyKey` |
| `presentation` | `CustodyOrderResponse.java` | MODIFICAR — expor `idempotencyKey` |
| `presentation` | `GlobalExceptionHandler.java` | MODIFICAR — handler para `InsufficientBalanceException` (422) e `OptimisticLockingFailureException` (409) |
| `domain` | `domain/exceptions/InsufficientBalanceException.java` | CRIAR |

**Sem impacto em:** Kafka (`OrderEvent` não muda), Redis (cache de assets inalterado), CI/CD, docker-compose, Terraform.

## Decisões Pendentes

- **D-1 (SELL com lock pessimista vs. otimista):** `@Version` (otimista) retorna 409 em conflito e força o cliente a tentar novamente; lock pessimista (`@Lock(PESSIMISTIC_WRITE)`) serializa as escritas mas aumenta contenção. Recomendação: começar com `@Version` por ser menos invasivo.
- **D-2 (idempotencyKey obrigatório ou opcional):** Tornar `idempotencyKey` obrigatório na requisição é mais seguro; opcional permite compatibilidade com clientes existentes. Recomendação: opcional, gerado pelo servidor se ausente.
- **D-3 (retorno 200 ou 201 em requisição duplicada):** Quando `idempotencyKey` já existe, retornar `200 OK` com a ordem existente (semântica de "já processado") ou `201 Created` novamente. Recomendação: `200 OK`.
