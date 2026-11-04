# custody/integridade-financeira Specification

## Purpose

Garante integridade, rastreabilidade e segurança das operações financeiras no custody-service: bloqueio de saldo insuficiente em ordens SELL, controle de concorrência otimista via @Version, idempotência de requisições via idempotencyKey, histórico imutável de movimentações em asset_movements, precisão monetária com BigDecimal e atomicidade transacional na criação de ordens.

## Requirements

### Requirement: Bloqueio de saldo insuficiente em ordens SELL

A plataforma DEVE rejeitar ordens SELL que excedam a posição atual do portfólio para evitar posições negativas e inconsistências de saldo.

#### Scenario: Ordem SELL aceita quando saldo é suficiente

- **DADO** existe um portfólio com id "port-1" pertencente ao usuário "user-1"
- **E** existe o ativo "PETR4" com id "asset-1"
- **E** o portfólio "port-1" possui 100 unidades de "PETR4" em portfolio_assets
- **QUANDO** o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 50, portfolioId "port-1", assetId "asset-1"
- **ENTÃO** a resposta tem status HTTP 201
- **E** a ordem é criada com status PENDING
- **E** uma entrada DEBIT de 50 unidades é registrada em asset_movements para o portfólio "port-1"

#### Scenario: Ordem SELL rejeitada quando saldo é exatamente zero

- **DADO** o portfólio "port-1" possui 0 unidades de "PETR4"
- **QUANDO** o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 1, portfolioId "port-1", assetId "asset-1"
- **ENTÃO** a resposta tem status HTTP 422
- **E** o corpo da resposta contém o campo "error" com mensagem descritiva
- **E** nenhuma ordem é criada em custody_orders
- **E** nenhuma entrada é criada em asset_movements

#### Scenario: Ordem SELL rejeitada quando quantidade solicitada excede o saldo

- **QUANDO** o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 150, portfolioId "port-1", assetId "asset-1"
- **ENTÃO** a resposta tem status HTTP 422
- **E** o corpo da resposta contém o campo "error" indicando saldo insuficiente
- **E** nenhuma ordem é criada em custody_orders

#### Scenario: Ordem SELL aceita na quantidade exata do saldo

- **QUANDO** o usuário envia POST /api/v1/custody/orders com tipo SELL, quantidade 100, portfolioId "port-1", assetId "asset-1"
- **ENTÃO** a resposta tem status HTTP 201
- **E** a ordem é criada com status PENDING

#### Scenario: Ordem BUY não é bloqueada por saldo insuficiente

- **DADO** o portfólio "port-1" possui 0 unidades de "PETR4"
- **QUANDO** o usuário envia POST /api/v1/custody/orders com tipo BUY, quantidade 200, portfolioId "port-1", assetId "asset-1"
- **ENTÃO** a resposta tem status HTTP 201
- **E** a ordem é criada com status PENDING
- **E** uma entrada CREDIT de 200 unidades é registrada em asset_movements para o portfólio "port-1"

---

### Requirement: Idempotência de ordens

Requisições repetidas com o mesmo idempotencyKey NÃO devem criar ordens duplicadas para garantir segurança em retentativas de rede.

#### Scenario: Primeira requisição com idempotencyKey cria a ordem

- **QUANDO** o usuário envia POST /api/v1/custody/orders com idempotencyKey "key-abc", tipo BUY, quantidade 10, portfolioId "port-1", assetId "asset-1"
- **ENTÃO** a resposta tem status HTTP 201
- **E** a ordem é salva com idempotencyKey "key-abc"

#### Scenario: Segunda requisição com mesmo idempotencyKey retorna a ordem existente

- **DADO** já existe uma ordem com idempotencyKey "key-abc" para portfolioId "port-1", assetId "asset-1", tipo BUY, quantidade 10
- **QUANDO** o usuário envia novamente POST /api/v1/custody/orders com idempotencyKey "key-abc"
- **ENTÃO** a resposta tem status HTTP 200
- **E** o corpo da resposta contém a mesma ordem já criada (mesmo id, status, createdAt)
- **E** nenhuma nova ordem é inserida em custody_orders
- **E** nenhuma nova entrada é inserida em asset_movements

#### Scenario: Requisições com idempotencyKeys diferentes criam ordens distintas

- **QUANDO** o usuário envia POST com idempotencyKey "key-abc", tipo BUY, quantidade 10
- **E** o usuário envia POST com idempotencyKey "key-xyz", tipo BUY, quantidade 10
- **ENTÃO** existem dois registros distintos em custody_orders
- **E** cada registro tem seu respectivo idempotencyKey

#### Scenario: Requisição sem idempotencyKey gera chave automaticamente no servidor

- **QUANDO** o usuário envia POST /api/v1/custody/orders sem o campo idempotencyKey
- **ENTÃO** a resposta tem status HTTP 201
- **E** a ordem criada possui um idempotencyKey não-nulo gerado pelo servidor

---

### Requirement: Controle de concorrência otimista em posições de portfólio

A plataforma DEVE detectar e rejeitar escritas simultâneas conflitantes em portfolio_assets para garantir que o saldo nunca seja calculado a partir de dados desatualizados.

#### Scenario: Duas ordens SELL simultâneas sobre o mesmo ativo conflitam

- **DADO** o portfólio "port-1" possui 100 unidades de "PETR4" com @Version = 1
- **QUANDO** duas requisições SELL de 60 unidades chegam simultaneamente para o mesmo portfólio e ativo
- **ENTÃO** exatamente uma das requisições recebe HTTP 201 (a que ganhou o lock otimista)
- **E** a outra requisição recebe HTTP 409
- **E** o saldo final em portfolio_assets é de 40 unidades (100 - 60), nunca negativo

#### Scenario: Requisição recebe 409 em conflito de versão e não cria ordem nem movimentação

- **DADO** o portfólio "port-1" tem version = 5 em portfolio_assets para "PETR4"
- **QUANDO** uma requisição tenta atualizar a posição com version = 4 (desatualizada)
- **ENTÃO** a resposta tem status HTTP 409
- **E** nenhuma ordem é criada
- **E** nenhuma movimentação é registrada

---

### Requirement: Histórico imutável de movimentações

Cada débito e crédito de posição DEVE ser registrado como uma entrada imutável para ter rastreabilidade completa e auditável de todas as movimentações.

#### Scenario: Ordem BUY gera entrada CREDIT em asset_movements

- **QUANDO** uma ordem BUY de 50 unidades de "PETR4" é aceita para o portfólio "port-1"
- **ENTÃO** uma entrada é criada em asset_movements com type=CREDIT, quantity=50, portfolioId="port-1", assetId="asset-1"
- **E** a entrada possui occurredAt com timestamp da operação
- **E** a entrada possui referenceOrderId igual ao id da ordem criada

#### Scenario: Ordem SELL gera entrada DEBIT em asset_movements

- **DADO** o portfólio "port-1" possui 100 unidades de "PETR4"
- **QUANDO** uma ordem SELL de 30 unidades de "PETR4" é aceita para o portfólio "port-1"
- **ENTÃO** uma entrada é criada em asset_movements com type=DEBIT, quantity=30

#### Scenario: Entradas em asset_movements não podem ser alteradas ou deletadas via API

- **DADO** existe uma entrada em asset_movements com id "mov-1"
- **QUANDO** o sistema tenta alterar qualquer campo de "mov-1" (simulado em teste de domínio)
- **ENTÃO** a operação é impossível porque AssetMovement não expõe setters após construção

#### Scenario: referenceOrderId é sempre preenchido e aponta para a ordem geradora

- **QUANDO** qualquer ordem aceita pelo sistema
- **ENTÃO** a entrada em asset_movements tem referenceOrderId igual ao id da CustodyOrder criada na mesma transação

#### Scenario: Duas ordens distintas geram duas entradas independentes em asset_movements

- **QUANDO** uma ordem BUY de 50 é aceita e depois uma ordem SELL de 20 é aceita para o mesmo portfólio e ativo
- **ENTÃO** existem duas entradas em asset_movements para (portfolioId, assetId): uma CREDIT 50 e uma DEBIT 20
- **E** o saldo atual em portfolio_assets reflete 50 - 20 = 30

---

### Requirement: Precisão monetária com BigDecimal

Todas as quantidades de ativos DEVEM usar BigDecimal para evitar erros de arredondamento inerentes a double e float.

#### Scenario: Quantidade com casas decimais é preservada sem arredondamento

- **QUANDO** uma ordem BUY de quantidade 0.00001 (cinco casas decimais) é enviada
- **ENTÃO** a quantidade armazenada em custody_orders e asset_movements é exatamente 0.00001
- **E** a quantidade não sofre truncamento ou arredondamento

#### Scenario: Cálculo de saldo com muitas casas decimais não perde precisão

- **DADO** o portfólio "port-1" possui 1.00003 unidades de "PETR4"
- **QUANDO** uma ordem SELL de 0.00001 unidade é aceita
- **ENTÃO** o saldo restante em portfolio_assets é exatamente 1.00002

#### Scenario: Campos de quantidade nunca usam tipos primitivos double ou float

- **ENTÃO** as entidades AssetMovementEntity, CustodyOrderEntity, PortfolioAssetEntity
- **E** os modelos de domínio AssetMovement, CustodyOrder, PortfolioAsset
- **E** os DTOs CreateOrderInput, OrderOutput
- declaram quantity como java.math.BigDecimal em todos os seus campos de quantidade

---

### Requirement: Atomicidade da transação de criação de ordem

A criação de ordem, atualização de posição e registro de movimentação DEVEM ocorrer em uma única transação de banco de dados para garantir que nunca haja estado parcialmente aplicado.

#### Scenario: Falha ao salvar a ordem reverte a atualização de posição e a movimentação

- **DADO** o portfólio "port-1" possui 100 unidades de "PETR4"
- **QUANDO** uma ordem SELL de 50 é enviada e a gravação em custody_orders falha (simulado)
- **ENTÃO** a posição em portfolio_assets permanece 100
- **E** nenhuma entrada existe em asset_movements para esta operação

#### Scenario: Falha ao registrar a movimentação reverte a ordem e a atualização de posição

- **DADO** o portfólio "port-1" possui 100 unidades de "PETR4"
- **QUANDO** uma ordem SELL de 50 é enviada e a gravação em asset_movements falha (simulado)
- **ENTÃO** a posição em portfolio_assets permanece 100
- **E** nenhum registro existe em custody_orders para esta operação

#### Scenario: Todas as etapas bem-sucedidas confirmam a transação

- **QUANDO** uma ordem SELL de 50 é enviada e todas as gravações têm sucesso
- **ENTÃO** a posição é debitada em portfolio_assets (de 100 para 50)
- **E** a ordem está em custody_orders com status PENDING
- **E** a movimentação DEBIT está em asset_movements
- **E** o evento Kafka é publicado após o commit da transação
