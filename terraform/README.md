# Terraform — Custody Portfolio

Infraestrutura AWS completa provisionada com Terraform.

## Arquitetura provisionada

```
Internet
    │
    ▼
ALB (Application Load Balancer)
    │
    ├── api-gateway     (porta 8080)
    ├── auth-service    (porta 8081)
    ├── custody-service (porta 8082)
    ├── event-processor (porta 8083)
    └── audit-service   (porta 8084)
            │
            ├── RDS PostgreSQL  (banco compartilhado)
            ├── MSK Kafka       (custody-orders-topic, processed-orders-topic)
            └── ElastiCache     (Redis — cache do custody-service)
```

## Pré-requisitos

- Terraform >= 1.7.0
- AWS CLI configurado (`aws configure`)
- Permissões IAM para criar VPC, ECS, RDS, MSK, ElastiCache, ECR, ALB

## Primeiro uso (uma única vez)

```bash
# 1. Cria o bucket S3 e tabela DynamoDB para estado remoto
make bootstrap

# 2. Cria terraform.tfvars com os valores reais
cp terraform.tfvars.example terraform.tfvars
# edite o arquivo com senha do banco e jwt_secret

# 3. Inicializa o Terraform
make init
```

## Deploy

```bash
# Ver o que será criado
make plan ENV=dev

# Aplicar
make apply
```

## Estrutura

```
terraform/
├── main.tf              ← provider AWS + backend S3
├── variables.tf         ← variáveis globais
├── outputs.tf           ← outputs (endpoint RDS, DNS ALB, etc)
├── infrastructure.tf    ← instancia todos os módulos
├── terraform.tfvars.example
├── Makefile
└── modules/
    ├── vpc/             ← VPC, subnets públicas/privadas, NAT Gateway
    ├── rds/             ← PostgreSQL RDS
    ├── msk/             ← MSK Kafka
    ├── elasticache/     ← ElastiCache Redis
    ├── ecr/             ← ECR repository por serviço
    ├── ecs-cluster/     ← ECS Cluster Fargate + IAM roles + CloudWatch
    ├── ecs-service/     ← Task Definition + ECS Service (reutilizado por todos)
    └── alb/             ← Application Load Balancer + Target Groups
```

## Secrets no AWS Secrets Manager

As senhas **nunca passam como variáveis de ambiente em texto puro**. Ficam no Secrets Manager e são injetadas pela task execution role:

```bash
# Criar os secrets (executar uma vez)
aws secretsmanager create-secret \
  --name "custody-portfolio/db-password" \
  --secret-string "sua_senha_aqui"

aws secretsmanager create-secret \
  --name "custody-portfolio/jwt-secret" \
  --secret-string "seu_jwt_secret_aqui"
```
