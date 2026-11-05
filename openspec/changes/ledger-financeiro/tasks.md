# Tasks

---

## 1. Domínio — novos tipos

- [ ] 1.1 Criar `custody-service/src/main/java/com/custody/custody_service/domain/enums/TransactionType.java` com valores `ORDER`, `TRANSFER`, `ADJUSTMENT`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.2 Criar `custody-service/src/main/java/com/custody/custody_service/domain/enums/TransactionStatus.java` com valores `PENDING`, `CONFIRMED`, `REJECTED`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.3 Criar `custody-service/src/main/java/com/custody/custody_service/domain/enums/EntryType.java` com valores `DEBIT`, `CREDIT`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.4 Criar `custody-service/src/main/java/com/custody/custody_service/domain/models/LedgerTransaction.java` como classe imutável: construtor all-args com `id`, `reference`, `transactionType`, `status`, `createdAt` — sem setters; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.5 Criar `custody-service/src/main/java/com/custody/custody_service/domain/models/LedgerEntry.java` como classe imutável: construtor all-args com `id`, `transactionId`, `accountId`, `assetId`, `entryType`, `quantity` (BigDecimal), `createdAt` — sem setters; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.6 Criar `custody-service/src/main/java/com/custody/custody_service/domain/models/IdempotencyRecord.java` como classe imutável: construtor all-args com `id`, `idempotencyKey`, `requestHash`, `responseSnapshot` (String), `createdAt` — sem setters; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.7 Criar `custody-service/src/main/java/com/custody/custody_service/domain/exceptions/IdempotencyKeyConflictException.java` estendendo `BusinessException`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.8 Criar `custody-service/src/main/java/com/custody/custody_service/domain/exceptions/InvalidScaleException.java` estendendo `BusinessException` com mensagem incluindo escala esperada e recebida; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.9 Criar `custody-service/src/main/java/com/custody/custody_service/domain/repositories/LedgerTransactionRepository.java` com métodos `save` e `findByReference`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.10 Criar `custody-service/src/main/java/com/custody/custody_service/domain/repositories/LedgerEntryRepository.java` com métodos `save` e `findByAccountIdAndAssetId(UUID, UUID, Pageable)` retornando `Page<LedgerEntry>`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.11 Criar `custody-service/src/main/java/com/custody/custody_service/domain/repositories/IdempotencyRecordRepository.java` com métodos `save` e `findByIdempotencyKey`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 1.12 Adicionar campo `Integer scale` (com default 5) em `custody-service/src/main/java/com/custody/custody_service/domain/models/Asset.java` com getter; verificar que `mvnw compile -pl custody-service` passa.

---

## 2. Testes unitários de domínio

- [ ] 2.1 Criar `custody-service/src/test/java/com/custody/custody_service/domain/models/LedgerTransactionTest.java` cobrindo: construção com todos os campos; imutabilidade (sem setters via reflexão); verificar que `mvnw test -pl custody-service -Dtest=LedgerTransactionTest` passa.

- [ ] 2.2 Criar `custody-service/src/test/java/com/custody/custody_service/domain/models/LedgerEntryTest.java` cobrindo: construção com todos os campos; `quantity` preserva escala; imutabilidade (sem setters); verificar que `mvnw test -pl custody-service -Dtest=LedgerEntryTest` passa.

- [ ] 2.3 Criar `custody-service/src/test/java/com/custody/custody_service/domain/models/IdempotencyRecordTest.java` cobrindo: construção; imutabilidade; verificar que `mvnw test -pl custody-service -Dtest=IdempotencyRecordTest` passa.

---

## 3. Infraestrutura — entidades e repositórios

- [ ] 3.1 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/entities/LedgerTransactionEntity.java` com `@Entity`, `@Table(name = "ledger_transactions")`, todos os campos `@Column(updatable = false)`, `@PrePersist` para `createdAt`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 3.2 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/entities/LedgerEntryEntity.java` com `@Entity`, `@Table(name = "ledger_entries", indexes = {...})`, todos os campos `@Column(updatable = false)`, `@PrePersist` para `createdAt`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 3.3 Criar `custody-service/src/main/java/com/custody/custody_service/infrastructure/persistence/entities/IdempotencyRecordEntity.java` com `@Entity`, `@Table(name = "idempotency_records")`, `@Column(unique = true)` em `idempotencyKey`, todos os campos `updatable = false`, `@PrePersist` para `createdAt`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 3.4 Adicionar campo `@Column(nullable = false) private Integer scale;` com default 5 em `AssetEntity.java`; atualizar `AssetMapper` para incluir `scale`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 3.5 Criar `SpringDataLedgerTransactionRepository.java`, `SpringDataLedgerEntryRepository.java` (com `findByAccountIdAndAssetIdOrderByCreatedAtDesc(UUID, UUID, Pageable)`), `SpringDataIdempotencyRecordRepository.java` (com `findByIdempotencyKey`); verificar que `mvnw compile -pl custody-service` passa.

- [ ] 3.6 Criar `LedgerTransactionMapper.java`, `LedgerEntryMapper.java`, `IdempotencyRecordMapper.java` com métodos estáticos `toEntity` e `toDomain`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 3.7 Criar `LedgerTransactionRepositoryImpl.java`, `LedgerEntryRepositoryImpl.java`, `IdempotencyRecordRepositoryImpl.java` implementando as interfaces de domínio; verificar que `mvnw compile -pl custody-service` passa.

---

## 4. Application — use case e DTOs

- [ ] 4.1 Criar `custody-service/src/main/java/com/custody/custody_service/application/dto/LedgerEntryOutput.java` e `LedgerStatementOutput.java` com campos de paginação; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 4.2 Criar `custody-service/src/main/java/com/custody/custody_service/application/usecases/GetLedgerStatementUseCase.java` com `@Transactional(readOnly = true)`, recebendo `portfolioId`, `assetId` (opcional), `page`, `size`; retornar `LedgerStatementOutput`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 4.3 Atualizar `CreateOrderUseCase.java`:
  - (a) Receber `idempotencyKey` como parâmetro separado (do header, não do input).
  - (b) Verificar `idempotencyRecordRepository.findByIdempotencyKey(key)` — se existe e hash coincide, retornar snapshot (200); se existe e hash difere, lançar `IdempotencyKeyConflictException` (409).
  - (c) Validar escala: `quantity.scale() <= asset.getScale()` — se exceder, lançar `InvalidScaleException` (422).
  - (d) Rejeitar NaN/Infinity: validar `Double.isFinite()` no controller antes de converter.
  - (e) Criar `LedgerTransaction` (reference = orderId, type = ORDER, status = CONFIRMED) e `LedgerEntry` (CREDIT para BUY, DEBIT para SELL).
  - (f) Manter gravação em `AssetMovement` (compatibilidade).
  - (g) Armazenar `IdempotencyRecord` com hash SHA-256 do body e responseSnapshot.
  - (h) Gerar `idempotencyKey = header != null ? header : UUID.randomUUID()`.
  - verificar que `mvnw compile -pl custody-service` passa.

---

## 5. Presentation — controller, request, response, exception handler

- [ ] 5.1 Modificar `CustodyOrderRequest.java` — remover campo `idempotencyKey` (move para header); verificar que `mvnw compile -pl custody-service` passa.

- [ ] 5.2 Modificar `CustodyController.java`:
  - (a) Extrair `Idempotency-Key` do header `@RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey`.
  - (b) Passar `idempotencyKey` como parâmetro separado para `CreateOrderUseCase.execute(input, idempotencyKey)`.
  - (c) Adicionar header `Idempotency-Key` na resposta.
  - (d) Adicionar endpoint `GET /api/v1/custody/ledger/statement` chamando `GetLedgerStatementUseCase`.
  - verificar que `mvnw compile -pl custody-service` passa.

- [ ] 5.3 Criar `LedgerStatementResponse.java` e `LedgerEntryResponse.java` em `presentation/response/`; verificar que `mvnw compile -pl custody-service` passa.

- [ ] 5.4 Atualizar `GlobalExceptionHandler.java`:
  - (a) Adicionar `@ExceptionHandler(IdempotencyKeyConflictException.class)` → 409 com mensagem "Idempotency-Key já utilizado com parâmetros diferentes".
  - (b) Adicionar `@ExceptionHandler(InvalidScaleException.class)` → 422 com mensagem de escala.
  - (c) Adicionar `@ExceptionHandler(PessimisticLockingFailureException.class)` → 409 com mensagem de retry.
  - verificar que `mvnw compile -pl custody-service` passa.

---

## 6. Testes de use case (unitários com Mockito)

- [ ] 6.1 Criar `custody-service/src/test/java/com/custody/custody_service/application/usecases/GetLedgerStatementUseCaseTest.java` cobrindo: extrato com resultados; extrato vazio; extrato sem assetId; paginação correta; verificar que `mvnw test -pl custody-service -Dtest=GetLedgerStatementUseCaseTest` passa.

- [ ] 6.2 Atualizar `CreateOrderUseCaseTest.java` para cobrir:
  - Idempotency-Key do header (não do body).
  - Replay com hash coincidente retorna snapshot.
  - Reuso de chave com body diferente lança `IdempotencyKeyConflictException`.
  - Escala do quantity excede asset.scale lança `InvalidScaleException`.
  - Chave nula gera UUID automaticamente.
  - LedgerTransaction e LedgerEntry criados junto com ordem.
  - verificar que `mvnw test -pl custody-service -Dtest=CreateOrderUseCaseTest` passa.

---

## 7. Testes de integração (Testcontainers)

- [ ] 7.1 Criar `custody-service/src/test/java/com/custody/custody_service/infrastructure/LedgerIntegrationTest.java` usando Testcontainers (postgres:16-alpine + cp-kafka:7.6.1); cobrir:
  - BUY cria LedgerTransaction + LedgerEntry CREDIT.
  - SELL cria LedgerTransaction + LedgerEntry DEBIT.
  - Reconstrução de posição: soma de entries = saldo em portfolio_assets.
  - Extrato paginado retorna entries ordenadas DESC.
  - Imutabilidade: UPDATE em ledger_entries não altera campos updatable=false.
  - verificar que `mvnw test -pl custody-service -Dtest=LedgerIntegrationTest` passa (requer Docker).

- [ ] 7.2 Criar `custody-service/src/test/java/com/custody/custody_service/infrastructure/IdempotencyIntegrationTest.java` usando Testcontainers; cobrir:
  - Primeira requisição cria IdempotencyRecord com hash.
  - Replay com mesmo body retorna 200 com snapshot.
  - Reuso com body diferente retorna 409.
  - 10 requisições simultâneas com mesma chave: 1 operação, 9 replays 200.
  - verificar que `mvnw test -pl custody-service -Dtest=IdempotencyIntegrationTest` passa (requer Docker).

---

## 8. Verificação final

- [ ] 8.1 Executar `mvnw compile -pl custody-service` e confirmar `BUILD SUCCESS` com 0 erros.

- [ ] 8.2 Executar `mvnw test -pl custody-service` e confirmar que todos os testes passam (7.1 e 7.2 requerem Docker).

- [ ] 8.3 Confirmar rastreabilidade: cada cenário da spec (`specs/custody/ledger-financeiro/spec.md`) está coberto por ao menos um teste nas classes criadas (ver matriz no design.md).

---

## Workflow follow-up

- Resolver Q-1 (conta de custódia como contrapartida) quando houver requisitos regulatórios.
- Avaliar migração de `asset_movements` para `ledger_entries` em change futura.
- Alinhar `event-processor` com o ledger (Q-3) antes do deploy.
- Arquivar esta change após aprovação e testes verdes: `/opsx-archive`.
