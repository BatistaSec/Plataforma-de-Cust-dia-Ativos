# Plataforma de Custódia de Ativos

Plataforma de custódia de ativos construída com **Java 21, Spring Boot, Clean Architecture e Arquitetura Orientada a Eventos (EDA)**.

## 🚀 Tecnologias e Stack
- **Backend**: Java 21, Spring Boot, Spring Security, JPA, PostgreSQL, Redis, Kafka
- **Qualidade/Testes**: JUnit, Mockito, Testcontainers, Integration Tests, E2E
- **Arquitetura & Design**: Clean Architecture, SOLID, DDD, Microservices, Event-driven
- **Cloud & Infraestrutura**: AWS, Docker, ECS, S3
- **DevOps**: GitHub Actions, CI/CD, SonarQube
- **Observabilidade**: Spring Boot Actuator, OpenTelemetry, Prometheus, Grafana, CloudWatch

## 🎯 Fases de Desenvolvimento (Status)

- [x] **Fase 1: Infraestrutura Base** (Docker Compose com PostgreSQL, Redis e Kafka).
- [x] **Fase 2: Auth Service** (Implementação base com JWT, Spring Security e rotas de login/registro).
- [x] **Fase 3: API Gateway** (Configuração de rotas e validação de JWT via Gateway).
- [x] **Fase 4: Custody Service** (Core da aplicação, DDD, Clean Architecture e emissão de eventos).
- [] **Fase 5: Event Processor** (Consumo de eventos Kafka e integrações assíncronas).
- [ ] **Fase 6: Observabilidade & DevOps** (Testes automatizados, pipelines CI/CD, Grafana).

## 🛠️ Como rodar localmente

1. Suba a infraestrutura base (Banco de Dados, Redis e Mensageria):
   ```bash
   docker-compose up -d
   ```
2. Acesse o diretório do serviço desejado (ex: `auth-service`) e rode o Spring Boot:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
