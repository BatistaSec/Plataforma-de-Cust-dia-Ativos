# Plataforma de Custódia de Ativos

Plataforma de custódia de ativos construída com **Java 21, Spring Boot, Clean Architecture e Arquitetura Orientada a Eventos (EDA)**.

## 🚀 Tecnologias e Stack
- **Backend**: Java 21, Spring Boot, Spring Security, JPA.
- **Mensageria e Banco de Dados**: Kafka, PostgreSQL, Redis.
- **Arquitetura**: Clean Architecture, SOLID, DDD, Microservices.
- **Infraestrutura Local**: Docker & Docker Compose.

## 🛠️ Como rodar a infraestrutura local
Certifique-se de ter o Docker e Docker Compose instalados.

Na raiz do projeto, execute:
```bash
docker-compose up -d
```

Isto subirá os seguintes serviços:
- **PostgreSQL** (porta 5432)
- **Redis** (porta 6379)
- **Kafka** em modo KRaft (porta 9092)
