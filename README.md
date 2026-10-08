# Plataforma de Custódia de Ativos

Plataforma de custódia de ativos construída com **Java 21, Spring Boot, Clean Architecture e Arquitetura Orientada a Eventos (EDA)**.

## 🚀 Tecnologias e Stack
- **Backend**: Java 21, Spring Boot, Spring Security, JPA, PostgreSQL, Redis, Kafka
- **Qualidade/Testes**: JUnit, Mockito, Testcontainers, Integration Tests, E2E
- **Arquitetura & Design**: Clean Architecture, SOLID, DDD, Microservices, Event-driven
- **Cloud & Infraestrutura**: AWS, Docker, ECS, S3
- **DevOps**: GitHub Actions, CI/CD, SonarQube
- **Observabilidade**: Spring Boot Actuator, OpenTelemetry, Prometheus, Grafana, CloudWatch

## 🏗️ Estrutura de Microsserviços

A plataforma é desenhada como um ecossistema assíncrono e robusto, composto por 4 microsserviços principais:

1. 🚪 **`api-gateway` (Porta: 8080)**: Ponto único de entrada (Spring Cloud Gateway, WebFlux). Roteia as requisições e atua como uma barreira inicial.
2. 🛡️ **`auth-service` (Porta: 8081)**: Serviço de Autenticação e Registro. Usa Spring Security e JWT para gerar tokens de acesso. Mantém isolamento dos usuários e perfis.
3. 💼 **`custody-service` (Porta: 8082)**: Core do negócio (DDD). Gerencia Ativos, Portfólios e a criação de Ordens de Custódia. Usa **Redis** para cache/performance e **Kafka** (Producer) para publicar eventos assíncronos (`OrderEvent`).
4. ⚙️ **`event-processor` (Porta: 8083)**: Worker/Processador assíncrono. Consome os eventos do Kafka de forma resiliente, processa as ordens aplicando regras de idempotência, salva o estado definitivo no PostgreSQL e publica eventos de processamento concluído (`ProcessedOrderEvent`).

## 🎯 Fases de Desenvolvimento (Status)

- [x] **Fase 1: Infraestrutura Base** (Docker Compose com PostgreSQL, Redis e Kafka).
- [x] **Fase 2: Auth Service** (Implementação base aplicando DDD, JWT, Spring Security e rotas de login/registro).
- [x] **Fase 3: API Gateway** (Configuração de rotas e validação de JWT via Gateway).
- [x] **Fase 4: Custody Service** (Core da aplicação, DDD, Clean Architecture e emissão de eventos).
- [x] **Fase 5: Event Processor** (Consumo de eventos Kafka, idempotência, persistência JPA, e produção de eventos de retorno).
- [ ] **Fase 6: Observabilidade & DevOps** (Testes automatizados, pipelines CI/CD, Grafana, Prometheus).
## 🔌 Endpoints Disponíveis

Você pode acessar os endpoints diretamente pela porta de cada serviço ou através do **API Gateway (8080)**.
*Obs: Apenas os endpoints de Auth são públicos. Os demais exigem que você envie o cabeçalho `Authorization: Bearer <seu_token>` gerado no login.*

### 🛡️ Auth Service (8081)
- 🔓 `POST /api/v1/auth/register` - Criação de um novo usuário.
- 🔓 `POST /api/v1/auth/login` - Autenticação e geração do token JWT.

### 💼 Custody Service (8082)
- 🔒 `POST /api/v1/custody/portfolios?userId={id}` - Cria um portfólio para o usuário.
- 🔒 `GET /api/v1/custody/portfolios/user/{id}` - Busca o portfólio do usuário.
- 🔒 `POST /api/v1/custody/assets?ticker={ticker}&name={name}` - Cadastra um novo ativo.
- 🔒 `GET /api/v1/custody/assets` - Lista os ativos disponíveis.
- 🔒 `POST /api/v1/custody/orders` - Cria uma nova ordem (dispara evento pro Kafka).

### ⚙️ Event Processor (8083)
- 🔒 `GET /api/v1/processed-orders/{orderId}` - Consulta o status e detalhes de uma ordem já processada.

## 🛠️ Como rodar localmente

1. Suba a infraestrutura base (Banco de Dados, Redis e Mensageria):
   ```bash
   docker-compose up -d
   ```
2. Compile os projetos (opcional, pode ser feito pela IDE):
   ```bash
   .\mvnw.cmd clean install
   ```
3. Suba os serviços usando sua IDE (IntelliJ / VSCode) ou via linha de comando dentro de cada diretório:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
