# Tasks

> **Pendências abertas antes de iniciar:**
> - **P-1** — Decidir se `id ASC` entra no ORDER BY como desempate (afeta Grupos 4 e 5).
> - **P-2** — Inventariar consumidores dos endpoints v1 antes do deploy (breaking change de formato).
>
> As tarefas abaixo podem ser executadas independentemente de P-1. Se P-1 for aprovado, as queries JPQL do Grupo 4 devem incluir `, e.id ASC` no ORDER BY.

---

## 1. Preparação e dependências de teste

- [ ] 1.1 Adicionar `testcontainers:junit-jupiter:1.19.7` e `testcontainers:postgresql:1.19.7` no `audit-service/pom.xml` e verificar que `mvnw dependency:resolve -pl audit-service` conclui sem erro.

- [ ] 1.2 Criar `audit-service/src/test/resources/application-test.yml` com configuração mínima para testes (datasource desabilitada para unitários; Testcontainers para integração) e verificar que `mvnw compile -pl audit-service` continua passando.

---

## 2. Domínio — novos tipos

- [ ] 2.1 Criar `domain/enums/FilterSubject.java` com valores `ORDER` e `PORTFOLIO`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 2.2 Criar `domain/models/Page.java` como record com campos `content`, `page`, `size`, `totalElements` e métodos calculados `totalPages()`, `isFirst()`, `isLast()`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 2.3 Criar `src/test/java/.../domain/models/PageTest.java` com testes unitários cobrindo: `totalElements=0 → totalPages=0`, divisão exata, divisão com resto, `page=0 → isFirst=true`, última página, `page > totalPages → isLast=true`; verificar que `mvnw test -pl audit-service -Dtest=PageTest` passa.

- [ ] 2.4 Criar `domain/query/AuditLogFilter.java` com campos imutáveis e validações no construtor (`page>=0`, `1<=size<=100`, `from<=to` quando ambos presentes); verificar que `mvnw compile -pl audit-service` passa.

- [ ] 2.5 Criar `src/test/java/.../domain/query/AuditLogFilterTest.java` cobrindo todos os paths de validação (page negativo, size zero, size negativo, size=101, size=1 aceito, size=100 aceito, from>to, from==to aceito, eventType null aceito); verificar que `mvnw test -pl audit-service -Dtest=AuditLogFilterTest` passa.

---

## 3. Domínio — interface do repositório

- [ ] 3.1 Adicionar o método `Page<AuditLog> findPaged(AuditLogFilter filter)` à interface `domain/repositories/AuditLogRepository.java` (importar `AuditLogFilter` de `domain.query` — não de `application.dto`); verificar que `mvnw compile -pl audit-service` passa.

---

## 4. Infraestrutura — queries e adapter

- [ ] 4.1 Adicionar as 4 queries JPQL em `infrastructure/persistence/repositories/SpringDataAuditLogRepository.java`: `findByPortfolioIdPaged`, `findByPortfolioIdAndEventTypePaged`, `findByOrderIdPaged`, `findByOrderIdAndEventTypePaged` — todas com `ORDER BY e.occurredAt DESC` e `Pageable`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 4.2 Adicionar os índices compostos em `infrastructure/persistence/entities/AuditLogEntity.java`: `(portfolio_id, occurred_at DESC)` e `(order_id, occurred_at DESC)`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 4.3 Implementar `findPaged(AuditLogFilter)` em `infrastructure/persistence/adapters/AuditLogRepositoryImpl.java` — seleção da query com base em `filter.getSubject()` e `filter.getEventType() != null`, conversão de `Spring Page` para `domain.models.Page<AuditLog>`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 4.4 Criar `src/test/java/.../infrastructure/AuditLogRepositoryImplIntegrationTest.java` usando Testcontainers (PostgreSQL 16-alpine); cobrir: 50 registros → página 0 de 20, página 2 de 10; filtro `eventType`; filtro `from`/`to` com limites inclusivos; combinação de filtros; resultado vazio; page além do total; ordenação `occurredAt DESC`; `assetId=null` para `ORDER_PROCESSED`; verificar que `mvnw test -pl audit-service -Dtest=AuditLogRepositoryImplIntegrationTest` passa.

---

## 5. Application — use cases paginados

- [ ] 5.1 Criar `application/usecases/GetPagedAuditLogsByPortfolioUseCase.java` com método `execute(UUID portfolioId, AuditEventType eventType, LocalDateTime from, LocalDateTime to, int page, int size) → Page<AuditLogOutput>` — constrói `AuditLogFilter` com `FilterSubject.PORTFOLIO` e delega para repositório; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 5.2 Criar `application/usecases/GetPagedAuditLogsByOrderUseCase.java` com lógica equivalente e `FilterSubject.ORDER`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 5.3 Criar `src/test/java/.../application/usecases/GetPagedAuditLogsByPortfolioUseCaseTest.java` com repositório mockado, cobrindo: delega filtro correto ao repositório; mapeia `Page<AuditLog>` para `Page<AuditLogOutput>`; propaga `IllegalArgumentException` do filtro; verificar que `mvnw test -pl audit-service -Dtest=GetPagedAuditLogsByPortfolioUseCaseTest` passa.

- [ ] 5.4 Criar `src/test/java/.../application/usecases/GetPagedAuditLogsByOrderUseCaseTest.java` com cobertura análoga; verificar que `mvnw test -pl audit-service -Dtest=GetPagedAuditLogsByOrderUseCaseTest` passa.

---

## 6. Presentation — response e controller

- [ ] 6.1 Criar `presentation/response/PagedAuditResponse.java` com campos `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last` e todos os getters; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 6.2 Atualizar `presentation/controllers/AuditController.java`: remover injeção dos use cases legados do construtor; injetar `GetPagedAuditLogsByPortfolioUseCase` e `GetPagedAuditLogsByOrderUseCase`; adicionar query params (`eventType`, `from`, `to`, `page`, `size`) com defaults e `@DateTimeFormat(iso=ISO.DATE_TIME)` nos parâmetros temporais; alterar tipo de retorno para `ResponseEntity<PagedAuditResponse>`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 6.3 Atualizar `presentation/controllers/GlobalExceptionHandler.java`: adicionar handler para `MethodArgumentTypeMismatchException` com enumeração dos valores aceitos; garantir que o handler 500 retorna apenas `{ "error": "Erro interno do servidor", "timestamp": "..." }` sem `ex.getMessage()`; verificar que `mvnw compile -pl audit-service` passa.

- [ ] 6.4 Criar `src/test/java/.../presentation/AuditControllerTest.java` usando `@WebMvcTest` + MockMvc; cobrir: chamada sem parâmetros → 200 com defaults; `size=101` → 400 com mensagem; `page=-1` → 400; `eventType=INVALIDO` → 400 com valores válidos; `from > to` → 400; resposta contém todos os campos do `PagedAuditResponse`; verificar que `mvnw test -pl audit-service -Dtest=AuditControllerTest` passa.

---

## 7. Verificação final

- [ ] 7.1 Executar `mvnw compile -pl audit-service` e confirmar saída `BUILD SUCCESS` com 0 erros.

- [ ] 7.2 Executar `mvnw test -pl audit-service` e confirmar que todos os testes novos passam (os testes de integração requerem Docker disponível para Testcontainers).

- [ ] 7.3 Confirmar rastreabilidade: cada cenário da spec (`specs/audit/consulta-paginada/spec.md`) está coberto por ao menos um teste nas classes criadas.

---

## Workflow follow-up

- Revisar pendências P-1 (desempate determinístico) e P-5 (timezone) e decidir se entram nesta change ou em changes separadas.
- Executar smoke test manual com `docker-compose up -d` e `audit-service` local.
- Comunicar a breaking change de formato da resposta aos consumidores dos endpoints (P-2) antes do deploy.
- Arquivar a change após aprovação e testes passando: `/opsx-archive`.
