# Spec — Ledger Financeiro

## Feature: Ledger contábil com transações e lançamentos

```gherkin
Feature: Ledger contábil com transações e lançamentos
  Como plataforma de custódia
  Quero registrar todas as movimentações por meio de lançamentos financeiros rastreáveis agrupados em transações
  Para garantir rastreabilidade completa e auditável de todas as operações

  Background:
    Given existe um portfólio com id "port-1" pertencente ao usuário "user-1"
    And existe um portfólio com id "port-2" pertencente ao usuário "user-2"
    And existe o ativo "PETR4" com id "asset-1"

  Scenario: Ordem BUY gera transação CONFIRMED com uma entrada CREDIT
    When o usuário envia POST /api/v1/custody/orders com tipo BUY, quantidade 50, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 201
    And existe uma LedgerTransaction com reference igual ao id da ordem criada
    And a transação tem transactionType = ORDER e status = CONFIRMED
    And existe uma LedgerEntry com entryType = CREDIT, quantity = 50, accountId = "port-1", assetId = "asset-1"
    And a LedgerEntry está associada à LedgerTransaction

  Scenario: Ordem SELL gera transação CONFIRMED com uma entrada DEBIT
    Given o portfólio "port-1" possui 100 unidades de "PETR4"
    When o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 30, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 201
    And existe uma LedgerTransaction com reference igual ao id da ordem criada
    And existe uma LedgerEntry com entryType = DEBIT, quantity = 30, accountId = "port-1", assetId = "asset-1"

  Scenario: Referência única por movimentação
    When duas ordens distintas são criadas para o mesmo portfólio e ativo
    Then existem duas LedgerTransactions distintas
    And cada transação tem sua própria reference (orderId)
    And nenhuma transação compartilha a mesma reference

  Scenario: Lançamentos confirmados não podem ser modificados
    Given existe uma LedgerEntry confirmada com id "entry-1" e quantity = 50
    When o sistema tenta alterar o campo quantity de "entry-1"
    Then a operação é impossível porque LedgerEntry tem @Column(updatable = false) em todos os campos de dados

  Scenario: Reconstrução de posição a partir dos lançamentos
    Given o portfólio "port-1" tem 3 lançamentos para "PETR4": CREDIT 100, CREDIT 50, DEBIT 30
    When o sistema consulta a posição reconstruída do ledger
    Then o saldo calculado é 120 (100 + 50 - 30)
    And o saldo corresponde ao valor materializado em portfolio_assets

  Scenario: Consulta de extrato retorna lançamentos paginados ordenados por createdAt DESC
    Given o portfólio "port-1" tem 5 lançamentos para "PETR4" em datas distintas
    When o usuário consulta GET /api/v1/custody/ledger/statement?portfolioId=port-1&assetId=asset-1&page=0&size=10
    Then a resposta tem status HTTP 200
    And o corpo contém uma lista de lançamentos ordenados do mais recente ao mais antigo
    And a resposta contém metadados de paginação (page, size, totalElements, totalPages)
```

---

## Feature: Balanceamento de partidas dobradas em transferências

```gherkin
Feature: Balanceamento de partidas dobradas em transferências
  Como plataforma de custódia
  Quero que transferências entre portfólios gerem lançamentos balanceados de débito e crédito
  Para garantir que nenhuma transferência seja parcialmente registrada

  Scenario: Transferência gera débito na origem e crédito no destino no mesmo ativo
    Given o portfólio "port-1" possui 100 unidades de "PETR4"
    And o portfólio "port-2" possui 0 unidades de "PETR4"
    When uma transferência de 10 unidades de "PETR4" de "port-1" para "port-2" é executada
    Then existe uma LedgerTransaction com transactionType = TRANSFER e status = CONFIRMED
    And existe uma LedgerEntry DEBIT de 10 para accountId = "port-1", assetId = "asset-1"
    And existe uma LedgerEntry CREDIT de 10 para accountId = "port-2", assetId = "asset-1"
    And ambas as entradas pertencem à mesma LedgerTransaction
    And a soma de débitos para "PETR4" iguala a soma de créditos (10 = 10)

  Scenario: Transferência parcialmente registrada é rejeitada
    Given o portfólio "port-1" possui 100 unidades de "PETR4"
    When uma transferência de 10 unidades é enviada e a gravação da entrada CREDIT falha
    Then a transação é revertida
    And nenhuma LedgerEntry existe para esta transferência
    And nenhuma LedgerTransaction existe para esta transferência
    And a posição em portfolio_assets permanece inalterada
```

---

## Feature: Precisão monetária com escala configurável por ativo

```gherkin
Feature: Precisão monetária com escala configurável por ativo
  Como plataforma de custódia
  Quero que cada ativo defina sua própria escala decimal
  Para garantir que valores sejam persistidos e validados com a precisão correta

  Scenario: Ativo com scale=5 aceita quantidade com 5 casas decimais
    Given o ativo "PETR4" tem scale = 5
    When o usuário envia uma ordem BUY com quantidade 0.00001
    Then a resposta tem status HTTP 201
    And a quantidade armazenada no ledger é exatamente 0.00001

  Scenario: Ativo com scale=0 rejeita quantidade decimal
    Given o ativo "AÇÃO_INT" tem scale = 0
    When o usuário envia uma ordem BUY com quantidade 10.5
    Then a resposta tem status HTTP 422
    And o corpo da resposta contém o campo "error" indicando que a escala máxima é 0

  Scenario: Quantidade NaN ou infinita é rejeitada na fronteira da API
    When o usuário envia uma ordem com quantidade "NaN" ou "Infinity"
    Then a resposta tem status HTTP 400
    And nenhuma operação financeira é executada

  Scenario: RoundingMode HalfEven é aplicado quando necessário
    Given o ativo "CRYPTO" tem scale = 8
    And a operação interna produz 0.123456789
    Then o valor persistido é 0.12345679 (arredondado com HalfEven na 8ª casa)

  Scenario: Nenhuma operação financeira usa double ou float
    Then as entidades LedgerEntryEntity, LedgerTransactionEntity, PortfolioAssetEntity
    And os modelos de domínio LedgerEntry, LedgerTransaction, PortfolioAsset
    declaram todos os campos de quantidade como java.math.BigDecimal
```

---

## Feature: Idempotência robusta via HTTP header

```gherkin
Feature: Idempotência robusta via HTTP header
  Como cliente da API
  Quero que requisições repetidas com o mesmo Idempotency-Key header não criem operações duplicadas
  Para garantir segurança em retentativas de rede

  Scenario: Primeira requisição com Idempotency-Key cria a operação
    When o usuário envia POST /api/v1/custody/orders com header Idempotency-Key "key-abc"
    Then a resposta tem status HTTP 201
    And existe um registro em idempotency_records com idempotencyKey = "key-abc"
    And o registro contém o hash SHA-256 do body da requisição
    And o registro contém o snapshot da resposta (responseSnapshot)

  Scenario: Repetição da mesma requisição retorna o resultado original
    Given já existe uma operação com Idempotency-Key "key-abc" e body hash H1
    When o usuário envia novamente POST /api/v1/custody/orders com header Idempotency-Key "key-abc" e mesmo body
    Then a resposta tem status HTTP 200
    And o corpo da resposta é idêntico ao responseSnapshot armazenado
    And nenhuma nova operação é criada

  Scenario: Reuso de Idempotency-Key com body diferente é rejeitado
    Given já existe uma operação com Idempotency-Key "key-abc" e body hash H1
    When o usuário envia POST /api/v1/custody/orders com header Idempotency-Key "key-abc" e body diferente
    Then a resposta tem status HTTP 409
    And o corpo da resposta contém "error" indicando que a chave foi usada com parâmetros diferentes

  Scenario: Requisição sem Idempotency-Key gera chave automaticamente
    When o usuário envia POST /api/v1/custody/orders sem o header Idempotency-Key
    Then a resposta tem status HTTP 201
    And a resposta inclui o header Idempotency-Key com um UUID gerado pelo servidor
    And existe um registro em idempotency_records com a chave gerada

  Scenario: Dez requisições simultâneas com a mesma chave produzem uma operação
    When dez requisições simultâneas chegam com o mesmo Idempotency-Key "key-simultaneo"
    Then exatamente uma operação é criada
    And uma requisição recebe HTTP 201
    As demais requisições recebem HTTP 200 com o mesmo resultado
```

---

## Feature: Concorrência com lock pessimista em transferências

```gherkin
Feature: Concorrência com lock pessimista em transferências
  Como plataforma de custódia
  Quero serializar escritas simultâneas sobre a mesma posição em transferências
  Para garantir que o saldo nunca seja calculado a partir de dados desatualizados

  Scenario: Duas transferências concorrentes para o mesmo ativo não consomem o mesmo saldo
    Given o portfólio "port-1" possui 100 unidades de "PETR4"
    And duas transferências de 60 unidades partem simultaneamente de "port-1"
    Then exatamente uma transferência é confirmada (201)
    And a outra transferência é rejeitada com HTTP 409
    And o saldo final de "port-1" é 40 (nunca negativo)

  Scenario: Deadlock em transferência cruzada é tratado com retry orientado
    Given o portfólio "port-1" transfere 10 de "PETR4" para "port-2"
    And simultaneamente o portfólio "port-2" transfere 5 de "PETR4" para "port-1"
    Then se ocorre deadlock, a resposta é HTTP 409 com mensagem orientando retry
    And nenhuma posição fica inconsistente
```

---

## Feature: Consulta de extrato do ledger

```gherkin
Feature: Consulta de extrato do ledger
  Como usuário autenticado
  Quero consultar o extrato de lançamentos de um portfólio e ativo
  Para auditar o histórico de movimentações

  Scenario: Extrato retorna lançamentos paginados
    Given o portfólio "port-1" tem 15 lançamentos para "PETR4"
    When o usuário consulta GET /api/v1/custody/ledger/statement?portfolioId=port-1&assetId=asset-1&page=0&size=10
    Then a resposta tem status HTTP 200
    And o corpo contém 10 lançamentos na página 0
    And os metadados indicam totalElements = 15, totalPages = 2

  Scenario: Extrato vazio retorna 200 com lista vazia
    Given o portfólio "port-3" não tem lançamentos
    When o usuário consulta GET /api/v1/custody/ledger/statement?portfolioId=port-3&assetId=asset-1
    Then a resposta tem status HTTP 200
    And o corpo contém uma lista vazia
    And os metadados indicam totalElements = 0

  Scenario: Extrato sem assetId retorna todos os lançamentos do portfólio
    Given o portfólio "port-1" tem lançamentos de múltiplos ativos
    When o usuário consulta GET /api/v1/custody/ledger/statement?portfolioId=port-1
    Then a resposta tem status HTTP 200
    And o corpo contém lançamentos de todos os ativos do portfólio
```
