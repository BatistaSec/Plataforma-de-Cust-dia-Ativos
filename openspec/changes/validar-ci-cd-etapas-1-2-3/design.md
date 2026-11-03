# Design

## Context

Ver `proposal.md — Why`. Os 5 workflows já estão escritos e estruturalmente corretos. O que falta é executá-los de verdade num PR real e verificar se passam. O principal risco é o job `2 · Tests` falhar por `contextLoads()` — esse teste tenta subir o contexto Spring Boot completo, que requer PostgreSQL e Kafka disponíveis, e os runners do GitHub Actions não têm esses serviços por padrão.

## Goals / Non-Goals

**Goals:**
- Abrir um PR de teste que dispara as etapas 1–3 nos 5 workflows
- Identificar e corrigir qualquer falha nos jobs validate-branch, build, test e security
- Garantir que `dorny/test-reporter@v1` tem permissão para escrever Checks no PR
- Documentar o resultado de cada serviço

**Non-Goals:**
- Implementar testes de integração com Testcontainers (escopo da change de auditoria)
- Configurar secrets AWS ou executar etapas 4–9
- Alterar lógica de negócio de qualquer serviço

## Decisions

### D-1 — Corrigir contextLoads() com @SpringBootTest desativado ou profile de teste

O teste `AuditServiceApplicationTests.contextLoads()` (e equivalentes nos outros serviços) falha no CI porque tenta conectar ao PostgreSQL e ao Kafka. Há duas opções:

**A (recomendada):** Adicionar `src/test/resources/application-test.yml` em cada serviço com configurações que evitam conexão real:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
  kafka:
    bootstrap-servers: localhost:9092
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
```
E anotar o teste com `@ActiveProfiles("test")`.

**B:** Anotar o `contextLoads()` com `@Disabled` com comentário explicativo.

A opção A é mais correta — mantém o teste funcional com dependências simuladas.

### D-2 — Permissão do dorny/test-reporter@v1

O `dorny/test-reporter@v1` precisa de permissão `checks: write` para publicar relatórios no PR. Sem isso, o step falha silenciosamente ou emite warning. Adicionar `permissions` no nível do job `test`:

```yaml
test:
  permissions:
    checks: write
    pull-requests: write
```

### D-3 — Estratégia de PR de teste

Criar uma branch `ci/validar-etapas-1-2-3` com uma mudança mínima (ex: atualizar comentário no README ou adicionar `application-test.yml`) para disparar todos os workflows via path filters.

## Arquivos a criar ou modificar

| Arquivo | Ação | Motivo |
|---|---|---|
| `api-gateway/src/test/resources/application-test.yml` | CRIAR | Evitar falha de contextLoads no CI |
| `auth-service/src/test/resources/application-test.yml` | CRIAR | Idem |
| `custody-service/src/test/resources/application-test.yml` | CRIAR | Idem |
| `event-processor/src/test/resources/application-test.yml` | CRIAR | Idem |
| `audit-service/src/test/resources/application-test.yml` | CRIAR | Idem |
| Cada `*ApplicationTests.java` | MODIFICAR | Adicionar `@ActiveProfiles("test")` |
| Cada `ci-cd-*.yml` job `test` | MODIFICAR | Adicionar `permissions: checks: write` |

## Open Questions

- **Q-1:** O `dorny/test-reporter@v1` no contexto de PR de fork tem limitações adicionais de permissão. Como este repositório é pessoal (não fork), isso não deve ser um problema — mas verificar após o primeiro run.
- **Q-2:** O `event-processor` e `audit-service` já têm `timeout-minutes: 20`. Se o `contextLoads` travar por 20 minutos, o job é cancelado com erro. A correção do D-1 elimina esse risco.
