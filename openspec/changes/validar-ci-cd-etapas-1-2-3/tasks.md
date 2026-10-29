# Tasks

## 1. Corrigir testes para rodar no CI (sem banco real)

- [x] 1.1 Criar `api-gateway/src/test/resources/application-test.yml` com datasource H2 e Kafka desativado; adicionar `@ActiveProfiles("test")` em `ApiGatewayApplicationTests.java`; verificar que `mvnw test -pl api-gateway` passa localmente.

- [x] 1.2 Criar `auth-service/src/test/resources/application-test.yml` com datasource H2 e `@ActiveProfiles("test")` em `AuthServiceApplicationTests.java`; verificar que `mvnw test -pl auth-service` passa localmente.

- [x] 1.3 Criar `custody-service/src/test/resources/application-test.yml` com datasource H2, Redis desativado e Kafka desativado; adicionar `@ActiveProfiles("test")` em `CustodyServiceApplicationTests.java`; verificar que `mvnw test -pl custody-service` passa localmente.

- [x] 1.4 Criar `event-processor/src/test/resources/application-test.yml` com datasource H2 e Kafka desativado; adicionar `@ActiveProfiles("test")` em `EventProcessorApplicationTests.java`; verificar que `mvnw test -pl event-processor` passa localmente.

- [x] 1.5 Criar `audit-service/src/test/resources/application-test.yml` com datasource H2 e Kafka desativado; adicionar `@ActiveProfiles("test")` em `AuditServiceApplicationTests.java`; verificar que `mvnw test -pl audit-service` passa localmente.

---

## 2. Adicionar permissões ao job de testes nos workflows

- [x] 2.1 Adicionar `permissions: checks: write, pull-requests: write` ao job `test` nos 5 arquivos `ci-cd-*.yml`; verificar que o YAML é válido com `yamllint` ou visualização no GitHub.

---

## 3. Abrir PR de teste e verificar os checks

- [ ] 3.1 Criar branch `ci/validar-etapas-1-2-3` com as alterações dos grupos 1 e 2; fazer push no GitHub.

- [ ] 3.2 Abrir PR com título válido `ci: validar etapas 1-2-3 do pipeline` — verificar que o job `1 · Validate Branch` passa com ✅ em todos os 5 workflows.

- [ ] 3.3 Verificar que o job `2 · Build` passa com ✅ em todos os 5 serviços (JAR gerado e armazenado como artefato).

- [ ] 3.4 Verificar que o job `2 · Tests` passa com ✅ em todos os 5 serviços e o relatório JUnit aparece na aba Checks do PR.

- [ ] 3.5 Verificar que o job `3 · Security & Quality` passa com ✅ em todos os 5 serviços (Gitleaks sem segredos + Trivy sem CRITICAL/HIGH com fix).

- [ ] 3.6 Verificar que os jobs docker, terraform, deploy, smoke e rollback aparecem como `skipped` (❌ não executaram) — confirmando que PR não faz deploy.

---

## 4. Documentar resultado

- [ ] 4.1 Registrar na descrição do PR o status final de cada serviço (✅ passou / ❌ falhou + motivo) e fechar o PR sem merge (é um PR de validação, não vai para main).

---

## Workflow follow-up

- Após validação bem-sucedida, arquivar esta change: `/opsx-archive`.
- Com os checks verdes confirmados, qualquer PR futuro com título Conventional Commits já está protegido pelas etapas 1–3.
