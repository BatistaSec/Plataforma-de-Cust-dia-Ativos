# OpenSpec — Consulta Paginada de Histórico de Auditoria

**ID:** AUDIT-001  
**Serviço:** audit-service  
**Versão:** 2.0.0  
**Status:** Em Revisão  
**Data de criação:** 2026-10-29  
**Data de revisão:** 2026-10-29  
**Revisado por:** Arquiteto de Software Sênior — auditoria completa do repositório  

---

## 1. Contexto

O `audit-service` registra todos os eventos do ciclo de vida de uma ordem — `ORDER_CREATED`, `ORDER_PROCESSED`, `ORDER_FAILED` — consumindo os tópicos Kafka `custody-orders-topic` e `processed-orders-topic`. Os endpoints originais retornavam listas completas sem paginação, filtro por tipo de evento ou janela temporal.

A funcionalidade foi parcialmente implementada. Esta especificação revisada reflete o estado real do código após o primeiro ciclo de implementação, corrige divergências encontradas e complementa com cenários ausentes.

### Divergências encontradas na versão 1.0.0

| Item | Especificação v1.0.0 | Realidade do código | Decisão |
|---|---|---|---|
| Localização de `AuditLogFilter` | `application/dto` | Movido para `domain/query` após revisão | Código está correto; spec deve refletir o pacote atual |
| Limite de página mencionado na spec | máx. `100` | Constante `MAX_PAGE_SIZE = 100` em `AuditLogFilter` | Consistente — manter |
| AC-5: "tamanho máximo de 400 registros" | Título menciona 400, body diz 100 | Constante é 100 | **Inconsistência na spec**: o título do cenário AC-5 diz "400" mas o teste espera 100. Corrigido nesta versão para 100 |
| Retrocompatibilidade | Spec afirma que não quebra clientes existentes | Response mudou de `array JSON` para envelope paginado no mesmo path/versão | **Breaking change real**: documentada explicitamente nesta versão |
| Use cases antigos | Spec diz que seriam mantidos no controller | Removidos do controller (correto), continuam como beans Spring | Beans legados existem sem consumidor no controller; candidatos a remoção futura |
| SDD menciona `GlobalExceptionHandler` sem handler 500 seguro | Spec v1.0.0 omitia | Handler 500 removeu `detail` do response body | Código está correto; spec atualizada |
| Ordenação determinística | Spec v1.0.0 não menciona critério de desempate | Query ordena apenas por `occurredAt DESC` | **Gap**: sem desempate por `id`, dois registros com mesmo timestamp podem trocar de posição entre páginas. Registrado como pendência |

---

## 2. Definição da Funcionalidade

> **Como** analista de compliance ou operação,  
> **Quero** consultar o histórico de auditoria de um portfólio ou de uma ordem com filtros opcionais por tipo de evento e janela temporal, recebendo os resultados de forma paginada,  
> **Para** investigar incidentes, auditar comportamento e exportar relatórios sem sobrecarregar o sistema.

### Endpoints

| Método | Path | Autenticação | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/audit/portfolio/{portfolioId}` | JWT obrigatório | Histórico paginado por portfólio |
| `GET` | `/api/v1/audit/order/{orderId}` | JWT obrigatório | Histórico paginado por ordem |

> **Nota de breaking change:** estes endpoints substituem os originais no mesmo path e versão de API. O formato da resposta mudou de `array JSON` para envelope paginado. Clientes que deserializavam como `List<AuditLogResponse>` precisam ser migrados antes do deploy em produção.

### Query parameters

| Parâmetro | Tipo | Obrigatório | Default | Validação | Descrição |
|---|---|---|---|---|---|
| `page` | `int` | Não | `0` | `>= 0` | Número da página (zero-indexed) |
| `size` | `int` | Não | `20` | `1 ≤ size ≤ 100` | Itens por página |
| `eventType` | `string` | Não | — | `ORDER_CREATED`, `ORDER_PROCESSED` ou `ORDER_FAILED` | Filtro por tipo de evento |
| `from` | `LocalDateTime` (ISO-8601) | Não | — | Deve ser ≤ `to` quando ambos presentes | Início da janela temporal (inclusivo) |
| `to` | `LocalDateTime` (ISO-8601) | Não | — | Deve ser ≥ `from` quando ambos presentes | Fim da janela temporal (inclusivo) |

**Formato de data aceito:** `yyyy-MM-ddTHH:mm:ss` (sem timezone — banco armazena em UTC via `LocalDateTime`)

### Ordenação

Os resultados são sempre ordenados por `occurredAt DESC` (mais recente primeiro). Não há suporte a ordenação dinâmica via parâmetro.

> **Pendência P-1:** Critério de desempate — registros com o mesmo `occurredAt` não têm ordenação determinística garantida. Recomenda-se adicionar `id ASC` como critério secundário. Aguarda decisão antes da implementação.

### Estrutura da resposta (`200 OK`)

```json
{
  "content": [
    {
      "id":          "550e8400-e29b-41d4-a716-446655440000",
      "orderId":     "uuid-da-ordem",
      "portfolioId": "uuid-do-portfolio",
      "assetId":     "uuid-do-ativo",
      "eventType":   "ORDER_FAILED",
      "payload":     "{\"orderId\":\"...\",\"status\":\"FAILED\"}",
      "occurredAt":  "2026-10-15T14:30:00"
    }
  ],
  "page":          0,
  "size":          10,
  "totalElements": 1,
  "totalPages":    1,
  "first":         true,
  "last":          true
}
```

> **Nota sobre `assetId`:** registros do tipo `ORDER_PROCESSED` e `ORDER_FAILED` gerados a partir do `processed-orders-topic` **não carregam `assetId`** (o `ProcessedOrderEvent` não tem esse campo). Esses registros terão `assetId: null` na resposta. Isso é comportamento esperado e documentado.

### Estrutura dos erros (`400 Bad Request`)

```json
{
  "error":     "Descrição do problema",
  "timestamp": "2026-10-29T14:00:00.123456789"
}
```

---

## 3. Critérios de Aceitação

> **Convenção:** IDs de portfólio e ordem são UUIDs. Nas condições iniciais abaixo, `P1`, `P2`, `O1` são aliases descritivos para UUIDs válidos.

---

### AC-1 — Paginação básica: primeira página

```gherkin
Feature: Consulta paginada de histórico de auditoria

  Scenario: AC-1 — Retorno correto da primeira página
    Given que existem 50 registros de auditoria para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1?page=0&size=20
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "content" contém exatamente 20 registros
    And o campo "totalElements" é 50
    And o campo "totalPages" é 3
    And o campo "first" é true
    And o campo "last" é false
    And o campo "page" é 0
    And o campo "size" é 20
```

---

### AC-2 — Paginação básica: última página

```gherkin
  Scenario: AC-2 — Retorno correto da última página com registros remanescentes
    Given que existem 50 registros de auditoria para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1?page=2&size=20
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "content" contém exatamente 10 registros
    And o campo "totalElements" é 50
    And o campo "totalPages" é 3
    And o campo "first" é false
    And o campo "last" é true
    And o campo "page" é 2
```

---

### AC-3 — Filtro por tipo de evento

```gherkin
  Scenario: AC-3 — Filtragem isolada por eventType
    Given que existem 10 registros ORDER_CREATED e 5 registros ORDER_FAILED para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1?eventType=ORDER_FAILED
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "totalElements" é 5
    And todos os registros em "content" têm "eventType" igual a "ORDER_FAILED"
    And nenhum registro em "content" tem "eventType" diferente de "ORDER_FAILED"
```

---

### AC-4 — Filtro por janela temporal: limites inclusivos

```gherkin
  Scenario: AC-4 — Janela temporal com limites inclusivos em ambas as extremidades
    Given que existem registros com "occurredAt" nos valores exatos:
      | 2026-09-30T23:59:59 |  (fora — anterior ao início)
      | 2026-10-01T00:00:00 |  (dentro — no limite inferior)
      | 2026-10-07T12:00:00 |  (dentro — no meio)
      | 2026-10-15T23:59:59 |  (dentro — no limite superior)
      | 2026-10-16T00:00:00 |  (fora — posterior ao fim)
    When o cliente faz GET /api/v1/audit/portfolio/P1?from=2026-10-01T00:00:00&to=2026-10-15T23:59:59
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "totalElements" é 3
    And todos os registros retornados têm "occurredAt" entre 2026-10-01T00:00:00 e 2026-10-15T23:59:59 inclusive
    And os registros com "occurredAt" 2026-09-30T23:59:59 e 2026-10-16T00:00:00 não aparecem
```

---

### AC-5 — Combinação de filtros

```gherkin
  Scenario: AC-5 — Combinação de eventType, from e size
    Given que existem no portfólio P1:
      | 3 registros ORDER_PROCESSED com occurredAt em 2026-10-05T10:00:00 |
      | 2 registros ORDER_CREATED   com occurredAt em 2026-10-05T10:00:00 |
      | 4 registros ORDER_PROCESSED com occurredAt em 2026-09-20T10:00:00 |
    When o cliente faz GET /api/v1/audit/portfolio/P1?eventType=ORDER_PROCESSED&from=2026-10-01T00:00:00&size=5
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And todos os registros em "content" são do tipo ORDER_PROCESSED
    And todos têm "occurredAt" >= 2026-10-01T00:00:00
    And o campo "totalElements" é 3
    And no máximo 5 registros são retornados em "content"
```

---

### AC-6 — Limite de tamanho de página excedido

```gherkin
  Scenario: AC-6 — Rejeição de size superior ao limite de 100
    When o cliente faz GET /api/v1/audit/portfolio/P1?size=101
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 400 Bad Request
    And o campo "error" contém "tamanho máximo de página é 100"
```

---

### AC-7 — Resultado vazio para portfólio sem registros

```gherkin
  Scenario: AC-7 — Resposta estruturada para conjunto vazio
    Given que não existem registros de auditoria para o portfólio P2
    When o cliente faz GET /api/v1/audit/portfolio/P2
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "content" é uma lista vazia []
    And o campo "totalElements" é 0
    And o campo "totalPages" é 0
    And o campo "first" é true
    And o campo "last" é true
```

> **Nota:** `first = true` e `last = true` simultaneamente quando não há registros é comportamento correto do `Page.isFirst()` e `Page.isLast()` do domínio (page=0, totalPages=0 → isLast retorna true pela guarda `totalPages() == 0`).

---

### AC-8 — Filtro por ordem com eventType

```gherkin
  Scenario: AC-8 — Consulta por orderId com filtro de eventType
    Given que existem 3 registros para a ordem O1:
      | eventType         |
      | ORDER_CREATED     |
      | ORDER_PROCESSED   |
      | ORDER_FAILED      |
    When o cliente faz GET /api/v1/audit/order/O1?eventType=ORDER_CREATED
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "totalElements" é 1
    And o único registro retornado tem "eventType" igual a "ORDER_CREATED"
```

---

### AC-9 — Ordenação padrão decrescente por data

```gherkin
  Scenario: AC-9 — Registros retornados em ordem decrescente de occurredAt
    Given que existem 3 registros para o portfólio P1 com occurredAt:
      | 2026-10-01T08:00:00 |
      | 2026-10-03T12:00:00 |
      | 2026-10-02T18:00:00 |
    When o cliente faz GET /api/v1/audit/portfolio/P1
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o primeiro registro em "content" tem "occurredAt" igual a "2026-10-03T12:00:00"
    And o segundo registro em "content" tem "occurredAt" igual a "2026-10-02T18:00:00"
    And o terceiro registro em "content" tem "occurredAt" igual a "2026-10-01T08:00:00"
```

---

### AC-10 — Autenticação obrigatória

```gherkin
  Scenario: AC-10 — Requisição sem token JWT é rejeitada
    When o cliente faz GET /api/v1/audit/portfolio/P1 sem o header Authorization
    Then o status da resposta é 401 Unauthorized
```

> **Nota de implementação:** a autenticação é aplicada pelo `AuthenticationFilter` do `api-gateway`, não pelo `audit-service` diretamente. Em testes de integração isolados do audit-service (sem o gateway), este cenário pode não se aplicar.

---

### AC-11 — Tipo de evento inválido

```gherkin
  Scenario: AC-11 — eventType desconhecido retorna erro descritivo
    When o cliente faz GET /api/v1/audit/portfolio/P1?eventType=INVALIDO
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 400 Bad Request
    And o campo "error" contém "Valor inválido 'INVALIDO' para o parâmetro 'eventType'"
    And o campo "error" lista os valores aceitos: ORDER_CREATED, ORDER_PROCESSED, ORDER_FAILED
```

---

### AC-12 — Número de página negativo

```gherkin
  Scenario: AC-12 — Rejeição de page negativo
    When o cliente faz GET /api/v1/audit/portfolio/P1?page=-1
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 400 Bad Request
    And o campo "error" contém "número de página não pode ser negativo"
```

---

### AC-13 — Tamanho de página zero ou negativo

```gherkin
  Scenario: AC-13 — Rejeição de size igual a zero
    When o cliente faz GET /api/v1/audit/portfolio/P1?size=0
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 400 Bad Request
    And o campo "error" contém "tamanho mínimo de página é 1"

  Scenario: AC-13b — Rejeição de size negativo
    When o cliente faz GET /api/v1/audit/portfolio/P1?size=-5
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 400 Bad Request
    And o campo "error" contém "tamanho mínimo de página é 1"
```

---

### AC-14 — Página além do total de páginas

```gherkin
  Scenario: AC-14 — Página válida além do total retorna content vazio com metadados corretos
    Given que existem 5 registros para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1?page=10&size=20
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "content" é uma lista vazia []
    And o campo "totalElements" é 5
    And o campo "totalPages" é 1
    And o campo "last" é true
```

---

### AC-15 — Data inicial posterior à data final

```gherkin
  Scenario: AC-15 — Rejeição de from posterior a to
    When o cliente faz GET /api/v1/audit/portfolio/P1?from=2026-10-31T00:00:00&to=2026-10-01T00:00:00
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 400 Bad Request
    And o campo "error" contém "'from' deve ser anterior ao 'to'"
```

---

### AC-16 — Somente `from` presente (sem `to`)

```gherkin
  Scenario: AC-16 — Filtro com apenas from aplica limite inferior sem limite superior
    Given que existem registros com occurredAt em 2026-09-01, 2026-10-01 e 2026-11-01
    When o cliente faz GET /api/v1/audit/portfolio/P1?from=2026-10-01T00:00:00
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And os registros retornados têm occurredAt >= 2026-10-01T00:00:00
    And o registro com occurredAt 2026-09-01 não aparece
    And os registros com occurredAt 2026-10-01 e 2026-11-01 aparecem
```

---

### AC-17 — Somente `to` presente (sem `from`)

```gherkin
  Scenario: AC-17 — Filtro com apenas to aplica limite superior sem limite inferior
    Given que existem registros com occurredAt em 2026-09-01, 2026-10-01 e 2026-11-01
    When o cliente faz GET /api/v1/audit/portfolio/P1?to=2026-10-01T00:00:00
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And os registros retornados têm occurredAt <= 2026-10-01T00:00:00
    And o registro com occurredAt 2026-11-01 não aparece
```

---

### AC-18 — Filtros todos ausentes retorna primeira página com 20 itens

```gherkin
  Scenario: AC-18 — Chamada sem parâmetros usa defaults page=0, size=20, sem filtros
    Given que existem 30 registros para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "content" contém exatamente 20 registros
    And o campo "page" é 0
    And o campo "size" é 20
    And o campo "totalElements" é 30
```

---

### AC-19 — Cálculo de totalPages

```gherkin
  Scenario: AC-19 — totalPages calculado corretamente para divisão exata
    Given que existem 100 registros para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1?size=20
      com o header "Authorization: Bearer <token-válido>"
    Then o campo "totalPages" é 5
    And o campo "totalElements" é 100

  Scenario: AC-19b — totalPages arredondado para cima quando não é divisão exata
    Given que existem 101 registros para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1?size=20
      com o header "Authorization: Bearer <token-válido>"
    Then o campo "totalPages" é 6
    And o campo "totalElements" é 101
```

---

### AC-20 — Campo assetId nulo em ORDER_PROCESSED/ORDER_FAILED

```gherkin
  Scenario: AC-20 — Registros de ORDER_PROCESSED não contêm assetId
    Given que existe um registro do tipo ORDER_PROCESSED para a ordem O1
      (originado do processed-orders-topic, que não carrega assetId)
    When o cliente faz GET /api/v1/audit/order/O1?eventType=ORDER_PROCESSED
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o único registro em "content" tem "assetId" nulo ou ausente
    And o campo "eventType" é "ORDER_PROCESSED"
```

---

### AC-21 — Tamanho de página no limite exato (100)

```gherkin
  Scenario: AC-21 — size igual ao limite máximo é aceito
    When o cliente faz GET /api/v1/audit/portfolio/P1?size=100
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "size" é 100
```

---

### AC-22 — size igual a 1 (limite mínimo)

```gherkin
  Scenario: AC-22 — size igual a 1 é aceito e retorna exatamente 1 registro por página
    Given que existem 3 registros para o portfólio P1
    When o cliente faz GET /api/v1/audit/portfolio/P1?size=1&page=0
      com o header "Authorization: Bearer <token-válido>"
    Then o status da resposta é 200 OK
    And o campo "content" contém exatamente 1 registro
    And o campo "totalPages" é 3
    And o campo "first" é true
    And o campo "last" é false
```

---

## 4. Restrições Não-Funcionais

| Requisito | Critério | Status |
|---|---|---|
| Limite de página | Máximo 100 registros por página (validado no servidor) | Implementado |
| Consistência | Leitura direta do banco PostgreSQL sem cache | Implementado |
| Breaking change | Formato da resposta mudou; migração de clientes necessária antes do deploy | **Pendente migração de clientes** |
| Autenticação | JWT validado pelo api-gateway antes de chegar ao audit-service | Implementado (gateway) |
| Ordenação determinística | Critério de desempate para registros com mesmo `occurredAt` | **Pendente decisão (P-1)** |
| Testes | Cobertura de uso cases, filtros, paginação e queries com banco real | **Ausente — zero testes novos** |

---

## 5. Pendências e Decisões Abertas

| ID | Descrição | Impacto | Responsável |
|---|---|---|---|
| P-1 | Adicionar `id ASC` como critério de desempate na query (quando dois registros têm mesmo `occurredAt`) | Paginação não determinística sem isso | Aguarda aprovação |
| P-2 | Migração de clientes do contrato anterior (array JSON → envelope paginado) | Deploy pode quebrar consumidores existentes | Product Owner / Equipe de integração |
| P-3 | Remoção dos use cases legados `GetAuditLogsByOrderUseCase` e `GetAuditLogsByPortfolioUseCase` (ainda existem como beans Spring sem consumidor no controller) | Dead code — baixo risco, mas mantém acoplamento | Próximo ciclo |
| P-4 | Adicionar H2 ou Testcontainers para testes de integração | Sem testes, a query JPQL e os índices não são verificados | Próximo ciclo |
| P-5 | Definir formato de timezone para o campo `occurredAt` na API (atualmente `LocalDateTime` sem tz) | Ambiguidade para clientes em fusos diferentes | Decisão de produto |
