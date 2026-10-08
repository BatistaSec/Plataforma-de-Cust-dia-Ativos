# Arquitetura do Auth Service (Clean Architecture)

Este documento explica de forma visual e estrutural como a **Clean Architecture** (Arquitetura Limpa) foi implementada neste projeto. A regra principal desta arquitetura é a **Regra da Dependência**: o código do centro não pode depender de nada que esteja do lado de fora.

---

## 🏗️ 1. Diagrama de Camadas (A Cebola)

Abaixo está a representação visual das camadas. A seta aponta para **quem depende de quem**. Note que o fluxo de dependência é sempre **de fora para dentro**. O Domínio (no centro) não depende de ninguém.

```mermaid
graph TD
    subgraph Presentation ["4. Presentation (Camada Externa)"]
        Controllers[Rest Controllers]
        DTOs_API[Request / Response DTOs]
    end

    subgraph Infrastructure ["3. Infrastructure (Frameworks)"]
        JPA[Spring Data JPA]
        Adapters[Repository Adapters]
        Security[Spring Security / JWT]
    end

    subgraph Application ["2. Application (Casos de Uso)"]
        UseCases[Use Cases - Regras de Aplicação]
    end

    subgraph Domain ["1. Domain (O Coração - Regras de Negócio)"]
        Models[Entidades / Models]
        Ports[Interfaces / Ports]
        Exceptions[Exceções do Domínio]
    end

    Presentation --> Application
    Infrastructure --> Application
    Infrastructure -.->|Implementa a Interface| Ports
    Application --> Domain
    
    style Domain fill:#e1f5fe,stroke:#03a9f4,stroke-width:2px
    style Application fill:#fff3e0,stroke:#ff9800,stroke-width:2px
    style Infrastructure fill:#e8f5e9,stroke:#4caf50,stroke-width:2px
    style Presentation fill:#f3e5f5,stroke:#9c27b0,stroke-width:2px
```

---

## 🔄 2. O Fluxo de uma Requisição (Como os dados caminham)

Como as coisas conversam na prática? Vamos ver o que acontece quando um usuário tenta fazer um "Registro" no sistema.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Controller as AuthController (Presentation)
    participant UseCase as RegisterUserUseCase (Application)
    participant Port as UserRepository (Domain Port)
    participant Adapter as UserRepositoryImpl (Infrastructure)
    participant DB as Postgres DB
    
    Cliente->>Controller: POST /api/v1/auth/register (AuthRequest)
    Note over Controller,UseCase: Controller valida JSON e passa os dados
    Controller->>UseCase: execute(RegisterInput)
    
    Note over UseCase,Port: UseCase aplica regras e pede para salvar
    UseCase->>Port: save(User puro)
    
    Note over Port,Adapter: A Interface é implementada pelo Adaptador
    Port->>Adapter: (Redirecionamento Injeção de Dependência)
    
    Note over Adapter,DB: Adaptador converte User para UserEntity (JPA)
    Adapter->>DB: INSERT INTO users ...
    DB-->>Adapter: Retorna UserEntity salvo
    
    Note over Adapter,Port: Adaptador converte de volta para User puro
    Adapter-->>Port: Retorna User
    Port-->>UseCase: Retorna User
    
    Note over UseCase,Controller: Monta DTO de resposta
    UseCase-->>Controller: Retorna AuthOutput
    Controller-->>Cliente: HTTP 200 OK (AuthResponse com Token)
```

---

## 📂 3. Entendendo a Estrutura de Pastas

Aqui está a tradução do diagrama para as pastas do nosso código:

### `domain/` (A Camada 1)
O código mais importante da empresa. Escrito em Java puro.
* **`models/User.java`**: A Entidade do negócio. Representa um usuário real. Não tem `@Entity` ou qualquer dependência do Spring.
* **`repositories/UserRepository.java`**: É o "Porto" (Port). Um contrato que diz: *"Alguém precisa salvar esse usuário, não me importa se é no banco, no papel ou num arquivo"*.

### `application/` (A Camada 2)
Onde acontece a "Coreografia" do sistema.
* **`usecases/RegisterUserUseCase.java`**: O passo a passo da regra de negócio (Verifica e-mail -> Cria usuário -> Manda criptografar a senha -> Manda salvar).
* **`dto/`**: Objetos burros que servem apenas para passar dados de uma camada para a outra.

### `infrastructure/` (A Camada 3)
A camada que fala com o mundo externo e com o Spring Boot.
* **`persistence/entities/UserEntity.java`**: É a classe "suja", com as anotações do Hibernate (`@Entity`, `@Table`) para mapear o banco de dados Postgres.
* **`persistence/adapters/UserRepositoryImpl.java`**: É o "Adaptador" (Adapter). Ele assina aquele contrato do Domínio (`UserRepository`). É ele quem faz o trabalho sujo de pegar o objeto Java puro, converter para Entidade JPA e mandar o Spring Data salvar.
* **`mappers/UserMapper.java`**: Responsável por fazer a tradução de `User` para `UserEntity` e vice-versa.

### `presentation/` (A Camada 4)
A porta de entrada do nosso sistema.
* **`controllers/AuthController.java`**: Recebe o sinal da Internet (HTTP), pega o JSON, valida os campos e entrega para a camada `Application` (UseCase) fazer o trabalho.
* **`controllers/GlobalExceptionHandler.java`**: Traduz erros do Java (ex: `UserNotFoundException`) para respostas HTTP bonitas (ex: Erro 404).

---

## 💡 Resumo Final

Graças a esta estrutura:
1. Se amanhã o Itaú decidir trocar o banco de dados (ex: MongoDB), **alteramos apenas a camada Infrastructure**. O Domínio e os Casos de Uso ficam intocados.
2. Se amanhã mudarmos de API REST (HTTP) para um bot do Telegram, **alteramos apenas a camada Presentation**. O resto não muda.
