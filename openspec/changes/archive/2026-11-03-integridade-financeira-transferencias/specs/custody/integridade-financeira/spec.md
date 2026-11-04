# Spec — Integridade Financeira e Transações

## Feature: Bloqueio de saldo insuficiente em ordens SELL

```gherkin
Feature: Bloqueio de saldo insuficiente em ordens SELL
  Como plataforma de custódia
  Quero rejeitar ordens SELL que excedam a posição atual do portfólio
  Para evitar posições negativas e inconsistências de saldo

  Background:
    Given existe um portfólio com id "port-1" pertencente ao usuário "user-1"
    And existe o ativo "PETR4" com id "asset-1"
    And o portfólio "port-1" possui 100 unidades de "PETR4" em portfolio_assets

  Scenario: Ordem SELL aceita quando saldo é suficiente
    When o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 50, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 201
    And a ordem é criada com status PENDING
    And uma entrada DEBIT de 50 unidades é registrada em asset_movements para o portfólio "port-1"

  Scenario: Ordem SELL rejeitada quando saldo é exatamente zero
    Given o portfólio "port-1" possui 0 unidades de "PETR4"
    When o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 1, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 422
    And o corpo da resposta contém o campo "error" com mensagem descritiva
    And nenhuma ordem é criada em custody_orders
    And nenhuma entrada é criada em asset_movements

  Scenario: Ordem SELL rejeitada quando quantidade solicitada excede o saldo
    When o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 150, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 422
    And o corpo da resposta contém o campo "error" indicando saldo insuficiente
    And nenhuma ordem é criada em custody_orders

  Scenario: Ordem SELL aceita na quantidade exata do saldo
    When o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 100, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 201
    And a ordem é criada com status PENDING

  Scenario: Ordem BUY não é bloqueada por saldo insuficiente
    Given o portfólio "port-1" possui 0 unidades de "PETR4"
    When o usuário envia POST /api/v1/custody/orders com tipo BUY, quantidade 200, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 201
    And a ordem é criada com status PENDING
    And uma entrada CREDIT de 200 unidades é registrada em asset_movements para o portfólio "port-1"
```

---

## Feature: Idempotência de ordens

```gherkin
Feature: Idempotência de ordens
  Como cliente da API
  Quero que requisições repetidas com o mesmo idempotencyKey não criem ordens duplicadas
  Para garantir segurança em retentativas de rede

  Scenario: Primeira requisição com idempotencyKey cria a ordem
    When o usuário envia POST /api/v1/custody/orders com idempotencyKey "key-abc", tipo BUY, quantidade 10, portfolioId "port-1", assetId "asset-1"
    Then a resposta tem status HTTP 201
    And a ordem é salva com idempotencyKey "key-abc"

  Scenario: Segunda requisição com mesmo idempotencyKey retorna a ordem existente
    Given já existe uma ordem com idempotencyKey "key-abc" para portfolioId "port-1", assetId "asset-1", tipo BUY, quantidade 10
    When o usuário envia novamente POST /api/v1/custody/orders com idempotencyKey "key-abc"
    Then a resposta tem status HTTP 200
    And o corpo da resposta contém a mesma ordem já criada (mesmo id, status, createdAt)
    And nenhuma nova ordem é inserida em custody_orders
    And nenhuma nova entrada é inserida em asset_movements

  Scenario: Requisições com idempotencyKeys diferentes criam ordens distintas
    When o usuário envia POST com idempotencyKey "key-abc", tipo BUY, quantidade 10
    And o usuário envia POST com idempotencyKey "key-xyz", tipo BUY, quantidade 10
    Then existem dois registros distintos em custody_orders
    And cada registro tem seu respectivo idempotencyKey

  Scenario: Requisição sem idempotencyKey gera chave automaticamente no servidor
    When o usuário envia POST /api/v1/custody/orders sem o campo idempotencyKey
    Then a resposta tem status HTTP 201
    And a ordem criada possui um idempotencyKey não-nulo gerado pelo servidor
```

---

## Feature: Controle de concorrência otimista em posições de portfólio

```gherkin
Feature: Controle de concorrência otimista em posições de portfólio
  Como plataforma de custódia
  Quero detectar e rejeitar escritas simultâneas conflitantes em portfolio_assets
  Para garantir que o saldo nunca seja calculado a partir de dados desatualizados

  Scenario: Duas ordens SELL simultâneas sobre o mesmo ativo conflitam
    Given o portfólio "port-1" possui 100 unidades de "PETR4" com @Version = 1
    When duas requisições SELL de 60 unidades chegam simultaneamente para o mesmo portfólio e ativo
    Then exatamente uma das requisições recebe HTTP 201 (a que ganhou o lock otimista)
    And a outra requisição recebe HTTP 409
    And o saldo final em portfolio_assets é de 40 unidades (100 - 60), nunca negativo

  Scenario: Requisição recebe 409 em conflito de versão e não cria ordem nem movimentação
    Given o portfólio "port-1" tem version = 5 em portfolio_assets para "PETR4"
    When uma requisição tenta atualizar a posição com version = 4 (desatualizada)
    Then a resposta tem status HTTP 409
    And nenhuma ordem é criada
    And nenhuma movimentação é registrada
```

---

## Feature: Histórico imutável de movimentações

```gherkin
Feature: Histórico imutável de movimentações de ativo
  Como plataforma de custódia
  Quero registrar cada débito e crédito de posição como uma entrada imutável
  Para ter rastreabilidade completa e auditável de todas as movimentações

  Scenario: Ordem BUY gera entrada CREDIT em asset_movements
    When uma ordem BUY de 50 unidades de "PETR4" é aceita para o portfólio "port-1"
    Then uma entrada é criada em asset_movements com type=CREDIT, quantity=50, portfolioId="port-1", assetId="asset-1"
    And a entrada possui occurredAt com timestamp da operação
    And a entrada possui referenceOrderId igual ao id da ordem criada

  Scenario: Ordem SELL gera entrada DEBIT em asset_movements
    Given o portfólio "port-1" possui 100 unidades de "PETR4"
    When uma ordem SELL de 30 unidades de "PETR4" é aceita para o portfólio "port-1"
    Then uma entrada é criada em asset_movements com type=DEBIT, quantity=30

  Scenario: Entradas em asset_movements não podem ser alteradas ou deletadas via API
    Given existe uma entrada em asset_movements com id "mov-1"
    When o sistema tenta alterar qualquer campo de "mov-1" (simulado em teste de domínio)
    Then a operação é impossível porque AssetMovement não expõe setters após construção

  Scenario: referenceOrderId é sempre preenchido e aponta para a ordem geradora
    When qualquer ordem aceita pelo sistema
    Then a entrada em asset_movements tem referenceOrderId igual ao id da CustodyOrder criada na mesma transação

  Scenario: Duas ordens distintas geram duas entradas independentes em asset_movements
    When uma ordem BUY de 50 é aceita e depois uma ordem SELL de 20 é aceita para o mesmo portfólio e ativo
    Then existem duas entradas em asset_movements para (portfolioId, assetId): uma CREDIT 50 e uma DEBIT 20
    And o saldo atual em portfolio_assets reflete 50 - 20 = 30
```

---

## Feature: Precisão monetária com BigDecimal

```gherkin
Feature: Precisão monetária com BigDecimal
  Como plataforma de custódia
  Quero que todas as quantidades de ativos usem BigDecimal
  Para evitar erros de arredondamento inerentes a double e float

  Scenario: Quantidade com casas decimais é preservada sem arredondamento
    When uma ordem BUY de quantidade 0.00001 (cinco casas decimais) é enviada
    Then a quantidade armazenada em custody_orders e asset_movements é exatamente 0.00001
    And a quantidade não sofre truncamento ou arredondamento

  Scenario: Cálculo de saldo com muitas casas decimais não perde precisão
    Given o portfólio "port-1" possui 1.00003 unidades de "PETR4"
    When uma ordem SELL de 0.00001 unidade é aceita
    Then o saldo restante em portfolio_assets é exatamente 1.00002

  Scenario: Campos de quantidade nunca usam tipos primitivos double ou float
    Then as entidades AssetMovementEntity, CustodyOrderEntity, PortfolioAssetEntity
    And os modelos de domínio AssetMovement, CustodyOrder, PortfolioAsset
    And os DTOs CreateOrderInput, OrderOutput
    declaram quantity como java.math.BigDecimal em todos os seus campos de quantidade
```

---

## Feature: Atomicidade da transação de criação de ordem

```gherkin
Feature: Atomicidade na criação de ordem
  Como plataforma de custódia
  Quero que a criação de ordem, atualização de posição e registro de movimentação
  ocorram em uma única transação de banco de dados
  Para garantir que nunca haja estado parcialmente aplicado

  Scenario: Falha ao salvar a ordem reverte a atualização de posição e a movimentação
    Given o portfólio "port-1" possui 100 unidades de "PETR4"
    When uma ordem SELL de 50 é enviada e a gravação em custody_orders falha (simulado)
    Then a posição em portfolio_assets permanece 100
    And nenhuma entrada existe em asset_movements para esta operação

  Scenario: Falha ao registrar a movimentação reverte a ordem e a atualização de posição
    Given o portfólio "port-1" possui 100 unidades de "PETR4"
    When uma ordem SELL de 50 é enviada e a gravação em asset_movements falha (simulado)
    Then a posição em portfolio_assets permanece 100
    And nenhum registro existe em custody_orders para esta operação

  Scenario: Todas as etapas bem-sucedidas confirmam a transação
    When uma ordem SELL de 50 é enviada e todas as gravações têm sucesso
    Then a posição é debitada em portfolio_assets (de 100 para 50)
    And a ordem está em custody_orders com status PENDING
    And a movimentação DEBIT está em asset_movements
    And o evento Kafka é publicado após o commit da transação
```
