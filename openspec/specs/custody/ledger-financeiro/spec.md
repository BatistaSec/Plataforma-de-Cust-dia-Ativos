# custody/ledger-financeiro Specification

## Purpose

Mantém um ledger contábil estruturado no custody-service: transações (`LedgerTransaction`) agrupando lançamentos de débito e crédito (`LedgerEntry`), idempotência robusta via HTTP header com hash SHA-256 e `responseSnapshot`, escala decimal configurável por ativo, e consulta de extrato paginada.

## Requirements

### Requirement: Ledger contábil com transações e lançamentos

Todas as movimentações DEVEM ser registradas como lançamentos financeiros rastreáveis agrupados em transações, garantindo rastreabilidade completa e auditável.

#### Scenario: Ordem BUY gera transação CONFIRMED com entrada CREDIT

- **DADO** existe um portfólio com id "port-1" e o ativo "PETR4" com id "asset-1"
- **QUANDO** o usuário envia POST /api/v1/custody/orders com tipo BUY, quantidade 50
- **ENTÃO** existe uma LedgerTransaction com reference igual ao id da ordem, transactionType = ORDER, status = CONFIRMED
- **E** existe uma LedgerEntry CREDIT de 50 unidades para accountId = "port-1", assetId = "asset-1"

#### Scenario: Ordem SELL gera transação com entrada DEBIT

- **DADO** o portfólio "port-1" possui 100 unidades de "PETR4"
- **QUANDO** o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 30
- **ENTÃO** existe uma LedgerEntry DEBIT de 30 unidades associada à LedgerTransaction

#### Scenario: Lançamentos confirmados não podem ser modificados

- **DADO** existe uma LedgerEntry confirmada
- **QUANDO** o sistema tenta alterar qualquer campo
- **ENTÃO** a operação é impossível porque LedgerEntry tem @Column(updatable = false)

#### Scenario: Reconstrução de posição a partir dos lançamentos

- **DADO** o portfólio "port-1" tem lançamentos CREDIT 100, CREDIT 50, DEBIT 30
- **QUANDO** o sistema consulta a posição reconstruída
- **ENTÃO** o saldo calculado é 120 (100 + 50 - 30)

#### Scenario: Consulta de extrato retorna lançamentos paginados

- **DADO** o portfólio "port-1" tem 15 lançamentos
- **QUANDO** o usuário consulta GET /api/v1/custody/ledger/statement?portfolioId=port-1&page=0&size=10
- **ENTÃO** a resposta tem status 200 com 10 lançamentos ordenados por createdAt DESC e metadados de paginação

---

### Requirement: Balanceamento de partidas dobradas em transferências

Transferências entre portfólios DEVEM gerar lançamentos balanceados de débito e crédito por ativo.

#### Scenario: Transferência gera débito na origem e crédito no destino

- **DADO** o portfólio "port-1" possui 100 unidades de "PETR4" e "port-2" possui 0
- **QUANDO** uma transferência de 10 unidades é executada
- **ENTÃO** existe uma LedgerEntry DEBIT de 10 para "port-1" e CREDIT de 10 para "port-2"
- **E** ambas pertencem à mesma LedgerTransaction e a soma de débitos iguala a soma de créditos

#### Scenario: Transferência parcialmente registrada é rejeitada

- **QUANDO** uma transferência é enviada e a gravação da entrada CREDIT falha
- **ENTÃO** a transação é revertida e nenhuma posição é alterada

---

### Requirement: Precisão monetária com escala configurável por ativo

Cada ativo DEVE definir sua própria escala decimal para garantir precisão correta.

#### Scenario: Ativo com scale=5 aceita 5 casas decimais

- **DADO** o ativo "PETR4" tem scale = 5
- **QUANDO** uma ordem BUY com quantidade 0.00001 é enviada
- **ENTÃO** a quantidade armazenada é exatamente 0.00001

#### Scenario: Ativo com scale=0 rejeita quantidade decimal

- **DADO** o ativo tem scale = 0
- **QUANDO** uma ordem com quantidade 10.5 é enviada
- **ENTÃO** a resposta tem status 422 indicando escala máxima é 0

#### Scenario: Nenhuma operação usa double ou float

- **ENTÃO** LedgerEntryEntity, LedgerTransactionEntity, PortfolioAssetEntity e todos os modelos de domínio declaram quantity como java.math.BigDecimal

---

### Requirement: Idempotência robusta via HTTP header

Requisições repetidas com o mesmo Idempotency-Key header NÃO devem criar operações duplicadas.

#### Scenario: Primeira requisição com Idempotency-Key cria a operação

- **QUANDO** o usuário envia POST /orders com header Idempotency-Key "key-abc"
- **ENTÃO** a resposta tem status 201 e existe um registro em idempotency_records com hash SHA-256 do body

#### Scenario: Repetição retorna o resultado original

- **DADO** já existe uma operação com Idempotency-Key "key-abc" e hash H1
- **QUANDO** o usuário envia novamente com a mesma chave e mesmo body
- **ENTÃO** a resposta tem status 200 e o corpo é idêntico ao responseSnapshot armazenado

#### Scenario: Reuso de chave com body diferente é rejeitado

- **DADO** já existe uma operação com Idempotency-Key "key-abc" e hash H1
- **QUANDO** o usuário envia com a mesma chave mas body diferente
- **ENTÃO** a resposta tem status 409 indicando que a chave foi usada com parâmetros diferentes

#### Scenario: Dez requisições simultâneas com a mesma chave produzem uma operação

- **QUANDO** dez requisições simultâneas chegam com o mesmo Idempotency-Key
- **ENTÃO** exatamente uma operação é criada e as demais recebem 200 com o mesmo resultado

---

### Requirement: Concorrência com lock em transferências

Escritas simultâneas sobre a mesma posição DEVEM ser detectadas e rejeitadas.

#### Scenario: Duas transferências concorrentes não consomem o mesmo saldo

- **DADO** o portfólio possui 100 unidades e duas transferências de 60 partem simultaneamente
- **ENTÃO** exatamente uma é confirmada (201) e a outra rejeitada (409)
- **E** o saldo final é 40, nunca negativo

#### Scenario: Deadlock é tratado com retry orientado

- **QUANDO** ocorre deadlock em transferência cruzada
- **ENTÃO** a resposta é 409 com mensagem orientando retry e nenhuma posição fica inconsistente

---

### Requirement: Consulta de extrato do ledger

O sistema DEVE fornecer consulta paginada de lançamentos por portfólio e ativo.

#### Scenario: Extrato retorna lançamentos paginados

- **DADO** o portfólio tem 15 lançamentos
- **QUANDO** consulta GET /ledger/statement?portfolioId=X&assetId=Y&page=0&size=10
- **ENTÃO** retorna 10 lançamentos na página 0 com totalElements = 15, totalPages = 2

#### Scenario: Extrato vazio retorna 200 com lista vazia

- **QUANDO** consulta um portfólio sem lançamentos
- **ENTÃO** retorna 200 com lista vazia e totalElements = 0

#### Scenario: Extrato sem assetId retorna todos os ativos

- **QUANDO** consulta GET /ledger/statement?portfolioId=X sem assetId
- **ENTÃO** retorna lançamentos de todos os ativos do portfólio
