# Tasks

> **Pendências a decidir antes de iniciar (ver design.md Q-1):**
> - **Q-1** — O `event-processor` ainda atualiza `portfolio_assets`? Se sim, o deploy desta change pode criar conflito de escrita dupla. Decidir antes de aplicar os Grupos 4 e 5.

---

## 1. Domínio — novos tipos

- [x] 1.1 Criar `custody-service/src/main/java/com/custody/custody_service/domain/enums/MovementType.java` com valores `DEBIT` e `CREDIT`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 1.2 Criar `custody-service/src/main/java/com/custody/custody_service/domain/exceptions/InsufficientBalanceException.java` estendendo `BusinessException` com mensagem que inclui saldo atual e quantidade solicitada; verificar que `mvnw compile -pl custody-service` passa.

- [x] 1.3 Criar `custody-service/src/main/java/com/custody/custody_service/domain/models/AssetMovement.java` como classe imutável: construtor all-args com `id`, `portfolioId`, `assetId`, `type` (MovementType), `quantity` (BigDecimal), `referenceOrderId`, `occurredAt` — sem nenhum setter público; verificar que `mvnw compile -pl custody-service` passa.

- [x] 1.4 Criar `custody-service/src/main/java/com/custody/custody_service/domain/repositories/AssetMovementRepository.java` com método `AssetMovement save(AssetMovement movement)`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 1.5 Adicionar método `debit(BigDecimal quantity)` em `custody-service/src/main/java/com/custody/custody_service/domain/models/PortfolioAsset.java`: lança `InsufficientBalanceException` se `this.quantity.compareTo(quantity) < 0`, caso contrário decrementa `this.quantity`; adicionar método `credit(BigDecimal quantity)` que soma; verificar que `mvnw compile -pl custody-service` passa.

- [x] 1.6 Adicionar campo `String idempotencyKey` em `custody-service/src/main/java/com/custody/custody_service/domain/models/CustodyOrder.java` com getter; atualizar o construtor principal para aceitar `idempotencyKey` como parâmetro opcional (sobrecarga ou nullable); verificar que `mvnw compile -pl custody-service` passa.

---

## 2. Testes unitários de domínio

- [x] 2.1 Criar `custody-service/src/test/java/com/custody/custody_service/domain/models/PortfolioAssetTest.java` cobrindo: `debit` com saldo suficiente; `debit` com saldo exato; `debit` com saldo insuficiente lança `InsufficientBalanceException`; `debit` com saldo zero lança exceção; `credit` soma corretamente; `debit` não é negativo após chamada bem-sucedida; verificar que `mvnw test -pl custody-service -Dtest=PortfolioAssetTest` passa.

- [x] 2.2 Criar `custody-service/src/test/java/com/custody/custody_service/domain/models/AssetMovementTest.java` cobrindo: construção com todos os campos; `quantity` preserva 5 casas decimais sem arredondamento; objeto não possui setters públicos (reflexão ou simples ausência de método); verificar que `mvnw test -pl custody-service -Dtest=AssetMovementTest` passa.

---

## 3. Infraestrutura — entidades e repositórios

- [x] 3.1 Adicionar `@Version private Long version;` com getter/setter em `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/entities/PortfolioAssetEntity.java`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.2 Adicionar campo `@Column(name = "idempotency_key", unique = true) private UUID idempotencyKey;` com getter/setter em `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/entities/CustodyOrderEntity.java`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.3 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/entities/AssetMovementEntity.java` com `@Entity`, `@Table(name = "asset_movements")`, todos os campos de dados com `@Column(updatable = false)`, `@PrePersist` para `occurredAt`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.4 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/repositories/SpringDataPortfolioAssetRepository.java` estendendo `JpaRepository<PortfolioAssetEntity, UUID>` com método `Optional<PortfolioAssetEntity> findByPortfolioIdAndAssetId(UUID portfolioId, UUID assetId)`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.5 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/repositories/SpringDataAssetMovementRepository.java` estendendo `JpaRepository<AssetMovementEntity, UUID>` e adicionando método `Optional<CustodyOrderEntity> findByIdempotencyKey(UUID key)` no `SpringDataCustodyOrderRepository` existente; verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.6 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/mappers/AssetMovementMapper.java` com métodos estáticos `toEntity(AssetMovement)` e `toDomain(AssetMovementEntity)`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.7 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/adapters/AssetMovementRepositoryImpl.java` implementando `AssetMovementRepository` (apenas `save`); verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.8 Registrar `SpringDataPortfolioAssetRepository` e `AssetMovementRepositoryImpl` como beans em `custody-service/src/main/java/com/custody/custody_service/infrastructure/config/BeanConfiguration.java`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 3.9 Adicionar o mapper `CustodyOrderMapper` para incluir `idempotencyKey` no mapeamento entity↔domain (se ainda não coberto); verificar que `mvnw compile -pl custody-service` passa.

---

## 4. Application — DTOs e use case

- [x] 4.1 Adicionar campo `UUID idempotencyKey` (nullable) em `custody-service/src/main/java/com/custody/custody_service/application/dto/CreateOrderInput.java` e em `OrderOutput.java`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 4.2 Atualizar `custody-service/src/main/java/com/custody/custody_service/application/usecases/CreateOrderUseCase.java`:
  - (a) Verificar `findByIdempotencyKey(input.getIdempotencyKey())` — se existir, retornar `200 OK` com ordem existente (sinalizar via retorno diferenciado para o controller — ex.: wrapper com flag `boolean alreadyExisted`).
  - (b) Resolver `PortfolioAsset` existente via `SpringDataPortfolioAssetRepository.findByPortfolioIdAndAssetId` (carregar para lock otimista).
  - (c) Para SELL: chamar `portfolioAsset.debit(quantity)` — propaga `InsufficientBalanceException`.
  - (d) Para BUY: chamar `portfolioAsset.credit(quantity)` (criar `PortfolioAsset` se ausente).
  - (e) Gerar `idempotencyKey = input.getIdempotencyKey() != null ? input.getIdempotencyKey() : UUID.randomUUID()`.
  - (f) Salvar `CustodyOrder` com `idempotencyKey`.
  - (g) Criar e salvar `AssetMovement` (DEBIT para SELL, CREDIT para BUY) com `referenceOrderId = order.getId()`.
  - (h) Salvar `PortfolioAsset` atualizado (aciona o check de `@Version`).
  - (i) Publicar `OrderEvent` após commit (mover `kafkaTemplate.send` para fora da transação se necessário, ou manter e aceitar o comportamento atual).
  - verificar que `mvnw compile -pl custody-service` passa.

---

## 5. Presentation — request, response, exception handler

- [x] 5.1 Adicionar campo `UUID idempotencyKey` (opcional) em `custody-service/src/main/java/com/custody/custody_service/presentation/request/CustodyOrderRequest.java` e em `CustodyOrderResponse.java`; verificar que `mvnw compile -pl custody-service` passa.

- [x] 5.2 Atualizar `custody-service/src/main/java/com/custody/custody_service/presentation/controllers/CustodyController.java`: no endpoint `POST /orders`, verificar se `CreateOrderUseCase` sinaliza replay e retornar `ResponseEntity.ok()` vs. `ResponseEntity.status(201).body(...)` conforme o caso; verificar que `mvnw compile -pl custody-service` passa.

- [x] 5.3 Atualizar `custody-service/src/main/java/com/custody/custody_service/presentation/controllers/GlobalExceptionHandler.java`:
  - Adicionar `@ExceptionHandler(InsufficientBalanceException.class)` → `422 UNPROCESSABLE_ENTITY` com `{ "error": mensagem, "timestamp": now }`.
  - Adicionar `@ExceptionHandler(ObjectOptimisticLockingFailureException.class)` → `409 CONFLICT` com `{ "error": "Conflito de concorrência...", "timestamp": now }`.
  - Garantir que nenhum handler expõe `ex.getMessage()` diretamente ao cliente nos casos de 500.
  - verificar que `mvnw compile -pl custody-service` passa.

---

## 6. Testes de use case (unitários com Mockito)

- [x] 6.1 Criar `custody-service/src/test/java/com/custody/custody_service/application/usecases/CreateOrderUseCaseTest.java` cobrindo:
  - SELL com saldo suficiente → `OrderOutput` com status PENDING e `idempotencyKey` preenchido.
  - SELL com saldo insuficiente → lança `InsufficientBalanceException`.
  - BUY sem posição prévia → cria `PortfolioAsset` e registra CREDIT.
  - Replay com `idempotencyKey` existente → retorna ordem existente sem chamar `save` novamente.
  - `idempotencyKey` nulo na entrada → servidor gera um UUID não-nulo.
  - Kafka só é chamado quando não é replay.
  - verificar que `mvnw test -pl custody-service -Dtest=CreateOrderUseCaseTest` passa.

---

## 7. Testes de integração (Testcontainers)

- [x] 7.1 Criar `custody-service/src/test/resources/application-test.yml` com PostgreSQL via Testcontainers e Kafka via Testcontainers (mesmo padrão do `event-processor`); verificar que `mvnw compile -pl custody-service` passa.

- [x] 7.2 Criar `custody-service/src/test/java/com/custody/custody_service/infrastructure/CreateOrderUseCaseIntegrationTest.java` usando Testcontainers (postgres:16-alpine + confluentinc/cp-kafka:7.6.1); cobrir:
  - SELL com saldo suficiente cria ordem + movimentação DEBIT + atualiza posição (assert no banco).
  - SELL com saldo insuficiente retorna 422, sem registro em nenhuma tabela.
  - Duas SELL simultâneas de 60 sobre posição de 100: uma passa (201), outra recebe 409, posição final = 40.
  - Replay com mesmo `idempotencyKey` → 200 OK, uma única ordem no banco.
  - BUY cria posição do zero + movimentação CREDIT.
  - Precisão de 5 casas decimais preservada end-to-end.
  - verificar que `mvnw test -pl custody-service -Dtest=CreateOrderUseCaseIntegrationTest` passa (requer Docker).

- [x] 7.3 Criar `custody-service/src/test/java/com/custody/custody_service/infrastructure/AssetMovementIntegrationTest.java` cobrindo imutabilidade (UPDATE bloqueado pela constraint `updatable=false`) e índice `(portfolio_id, asset_id, occurred_at DESC)` existindo em information_schema; verificar que `mvnw test -pl custody-service -Dtest=AssetMovementIntegrationTest` passa.

---

## 8. Verificação final

- [x] 8.1 Executar `mvnw compile -pl custody-service` e confirmar `BUILD SUCCESS` com 0 erros.

- [x] 8.2 Executar `mvnw test -pl custody-service` e confirmar que todos os testes novos passam (7.2 e 7.3 requerem Docker).

- [x] 8.3 Confirmar rastreabilidade: cada cenário da spec (`specs/custody/integridade-financeira/spec.md`) está coberto por ao menos um teste nas classes criadas (ver matriz no design.md).

---

## Workflow follow-up

- Resolver Q-1 (event-processor e portfolio_assets) antes do deploy em produção.
- Comunicar breaking change de `CustodyOrderResponse` (novo campo `idempotencyKey`) para consumidores da API antes do deploy.
- Arquivar esta change após aprovação e testes verdes: `/opsx-archive`.
