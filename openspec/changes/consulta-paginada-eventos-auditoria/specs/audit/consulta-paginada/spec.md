# Spec Delta

## Purpose

Capacidade do `audit-service` de retornar o histórico de auditoria de ordens e portfólios de forma paginada, com filtros opcionais por tipo de evento e janela temporal, ordenado por data decrescente — tornando a consulta viável para portfólios com histórico extenso.

## ADDED Requirements

### Requirement: Paginação obrigatória nas consultas de auditoria
O sistema DEVE retornar resultados paginados com envelope contendo `content`, `page`, `size`, `totalElements`, `totalPages`, `first` e `last`. `page` é zero-indexed, default `0`. `size` default `20`, mínimo `1`, máximo `100`.

#### Scenario: Primeira página com registros suficientes
- **GIVEN** existem 50 registros de auditoria para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?page=0&size=20` com token JWT válido
- **THEN** a resposta tem status `200 OK`
- **AND** `content` contém exatamente 20 registros
- **AND** `totalElements` é `50`, `totalPages` é `3`, `first` é `true`, `last` é `false`

#### Scenario: Última página com registros remanescentes
- **GIVEN** existem 50 registros para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?page=2&size=20` com token JWT válido
- **THEN** `content` contém exatamente 10 registros
- **AND** `last` é `true`, `first` é `false`, `totalPages` é `3`

#### Scenario: Chamada sem parâmetros usa defaults
- **GIVEN** existem 30 registros para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1` com token JWT válido
- **THEN** `content` contém 20 registros, `page` é `0`, `size` é `20`

#### Scenario: Resultado vazio retorna envelope estruturado
- **GIVEN** não existem registros de auditoria para o portfólio P2
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P2` com token JWT válido
- **THEN** status `200 OK`, `content` é `[]`, `totalElements` é `0`, `totalPages` é `0`, `first` é `true`, `last` é `true`

#### Scenario: Página além do total retorna content vazio com metadados corretos
- **GIVEN** existem 5 registros para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?page=10&size=20` com token JWT válido
- **THEN** status `200 OK`, `content` é `[]`, `totalElements` é `5`, `totalPages` é `1`, `last` é `true`

### Requirement: Cálculo determinístico de totalPages
O sistema DEVE calcular `totalPages` como `ceil(totalElements / size)`. Quando `totalElements` é `0`, `totalPages` DEVE ser `0`.

#### Scenario: Divisão exata
- **GIVEN** existem 100 registros para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?size=20`
- **THEN** `totalPages` é `5`

#### Scenario: Divisão com resto
- **GIVEN** existem 101 registros para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?size=20`
- **THEN** `totalPages` é `6`

### Requirement: Rejeição de parâmetros de paginação inválidos
O sistema DEVE retornar `400 Bad Request` com campo `error` descritivo para: `page < 0`, `size < 1`, `size > 100`.

#### Scenario: page negativo
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?page=-1` com token JWT válido
- **THEN** status `400 Bad Request`, `error` contém "número de página não pode ser negativo"

#### Scenario: size zero
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?size=0` com token JWT válido
- **THEN** status `400 Bad Request`, `error` contém "tamanho mínimo de página é 1"

#### Scenario: size negativo
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?size=-5` com token JWT válido
- **THEN** status `400 Bad Request`, `error` contém "tamanho mínimo de página é 1"

#### Scenario: size acima do limite
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?size=101` com token JWT válido
- **THEN** status `400 Bad Request`, `error` contém "tamanho máximo de página é 100"

#### Scenario: size exatamente no limite é aceito
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?size=100` com token JWT válido
- **THEN** status `200 OK`, `size` é `100`

#### Scenario: size mínimo (1) é aceito
- **GIVEN** existem 3 registros para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?size=1&page=0` com token JWT válido
- **THEN** status `200 OK`, `content` contém 1 registro, `totalPages` é `3`

### Requirement: Filtro por tipo de evento
O sistema DEVE aceitar o parâmetro opcional `eventType` com valores `ORDER_CREATED`, `ORDER_PROCESSED` ou `ORDER_FAILED`. Quando presente, apenas registros do tipo informado DEVEM aparecer em `content` e `totalElements` DEVE refletir somente esse subconjunto.

#### Scenario: Filtro retorna apenas o tipo solicitado
- **GIVEN** existem 10 registros `ORDER_CREATED` e 5 registros `ORDER_FAILED` para o portfólio P1
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?eventType=ORDER_FAILED` com token JWT válido
- **THEN** `totalElements` é `5`
- **AND** todos os registros em `content` têm `eventType` igual a `ORDER_FAILED`

#### Scenario: Filtro por ordem com eventType
- **GIVEN** existem 3 registros para a ordem O1: `ORDER_CREATED`, `ORDER_PROCESSED`, `ORDER_FAILED`
- **WHEN** o cliente faz `GET /api/v1/audit/order/O1?eventType=ORDER_CREATED` com token JWT válido
- **THEN** `totalElements` é `1`, o único registro tem `eventType` igual a `ORDER_CREATED`

#### Scenario: eventType inválido retorna 400 descritivo
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?eventType=INVALIDO` com token JWT válido
- **THEN** status `400 Bad Request`
- **AND** `error` contém "Valor inválido 'INVALIDO' para o parâmetro 'eventType'"
- **AND** `error` lista os valores aceitos: `ORDER_CREATED`, `ORDER_PROCESSED`, `ORDER_FAILED`

### Requirement: Filtro por janela temporal com limites inclusivos
O sistema DEVE aceitar os parâmetros opcionais `from` e `to` no formato `yyyy-MM-ddTHH:mm:ss`. Os limites DEVEM ser inclusivos. Quando apenas um dos dois é informado, o outro extremo é aberto.

#### Scenario: Limites temporais inclusivos
- **GIVEN** existem registros com `occurredAt` em: `2026-09-30T23:59:59` (fora), `2026-10-01T00:00:00` (dentro — limite), `2026-10-07T12:00:00` (dentro), `2026-10-15T23:59:59` (dentro — limite), `2026-10-16T00:00:00` (fora)
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?from=2026-10-01T00:00:00&to=2026-10-15T23:59:59`
- **THEN** `totalElements` é `3`, apenas os registros dentro do intervalo aparecem

#### Scenario: Apenas from — limite inferior sem limite superior
- **GIVEN** existem registros com `occurredAt` em 2026-09-01, 2026-10-01 e 2026-11-01
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?from=2026-10-01T00:00:00`
- **THEN** apenas os registros com `occurredAt >= 2026-10-01T00:00:00` aparecem

#### Scenario: Apenas to — limite superior sem limite inferior
- **GIVEN** existem registros com `occurredAt` em 2026-09-01, 2026-10-01 e 2026-11-01
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?to=2026-10-01T00:00:00`
- **THEN** apenas os registros com `occurredAt <= 2026-10-01T00:00:00` aparecem

#### Scenario: from posterior a to retorna 400
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?from=2026-10-31T00:00:00&to=2026-10-01T00:00:00`
- **THEN** status `400 Bad Request`, `error` contém "'from' deve ser anterior ao 'to'"

### Requirement: Combinação de filtros
O sistema DEVE aplicar todos os filtros informados simultaneamente. `totalElements` e `totalPages` DEVEM refletir o conjunto filtrado.

#### Scenario: eventType + from + size combinados
- **GIVEN** existem no portfólio P1: 3 registros `ORDER_PROCESSED` em 2026-10-05, 2 registros `ORDER_CREATED` em 2026-10-05, 4 registros `ORDER_PROCESSED` em 2026-09-20
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1?eventType=ORDER_PROCESSED&from=2026-10-01T00:00:00&size=5`
- **THEN** `totalElements` é `3`, todos os registros em `content` são `ORDER_PROCESSED` com `occurredAt >= 2026-10-01T00:00:00`

### Requirement: Ordenação padrão decrescente por occurredAt
O sistema DEVE retornar os registros ordenados por `occurredAt DESC`. A ordenação NÃO é configurável via parâmetro de query.

#### Scenario: Registros ordenados do mais recente para o mais antigo
- **GIVEN** existem 3 registros para o portfólio P1 com `occurredAt`: 2026-10-01, 2026-10-03, 2026-10-02
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1`
- **THEN** o primeiro registro tem `occurredAt` 2026-10-03, o segundo 2026-10-02, o terceiro 2026-10-01

### Requirement: Autenticação JWT obrigatória
O sistema DEVE exigir token JWT válido no header `Authorization: Bearer <token>`. Requisições sem token DEVEM ser rejeitadas com `401 Unauthorized` antes de chegar ao `audit-service`.

#### Scenario: Requisição sem token é rejeitada pelo gateway
- **WHEN** o cliente faz `GET /api/v1/audit/portfolio/P1` sem o header `Authorization`
- **THEN** status `401 Unauthorized` (retornado pelo `api-gateway`, não pelo `audit-service`)

### Requirement: Campo assetId nulo para eventos sem ativo associado
O sistema DEVE retornar `assetId` como `null` nos registros originados do `processed-orders-topic` (`ORDER_PROCESSED`, `ORDER_FAILED`), pois o `ProcessedOrderEvent` não carrega esse campo.

#### Scenario: ORDER_PROCESSED não contém assetId
- **GIVEN** existe um registro `ORDER_PROCESSED` para a ordem O1 (originado do `processed-orders-topic`)
- **WHEN** o cliente faz `GET /api/v1/audit/order/O1?eventType=ORDER_PROCESSED`
- **THEN** o único registro em `content` tem `assetId` nulo

### Requirement: Compatibilidade com clientes existentes
Os endpoints substituem os originais no mesmo path e versão. Clientes que deserializavam como `List<AuditLogResponse>` DEVEM ser migrados antes do deploy. Chamadas sem parâmetros retornam a primeira página de 20 itens.

#### Scenario: Chamada sem parâmetros mantém comportamento de dados (não de formato)
- **GIVEN** existem registros para o portfólio P1
- **WHEN** um cliente legado faz `GET /api/v1/audit/portfolio/P1` sem parâmetros
- **THEN** status `200 OK` com envelope paginado (não mais array JSON)
- **AND** os dados retornados correspondem aos primeiros 20 registros ordenados por `occurredAt DESC`

#### Scenario: Erro 500 não vaza detalhes internos
- **WHEN** ocorre uma falha de persistência (ex.: banco indisponível)
- **THEN** status `500 Internal Server Error`
- **AND** o corpo contém apenas `{ "error": "Erro interno do servidor", "timestamp": "..." }`
- **AND** nenhum stack trace, nome de classe ou mensagem de SQL aparece na resposta
