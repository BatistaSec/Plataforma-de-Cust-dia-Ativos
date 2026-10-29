# Proposal

## Why

Os workflows de CI/CD dos 5 microsserviços estão escritos e versionados, mas ainda não foram validados no GitHub Actions com um PR real. As etapas 1 (Validate Branch), 2 (Build + Tests) e 3 (Security: Gitleaks + Trivy) precisam passar de verdade antes de qualquer deploy, pois são o gate que protege a branch `main` contra código com título inválido, falhas de build, testes quebrados ou vulnerabilidades críticas.

## What Changes

- Criar um PR de teste no repositório `BatistaSec/Plataforma-de-Cust-dia-Ativos` com título seguindo Conventional Commits para disparar os workflows dos 5 serviços.
- Verificar que todos os jobs `1 · Validate Branch`, `2 · Build`, `2 · Tests` e `3 · Security & Quality` passam com ✅ verde no GitHub Actions.
- Corrigir qualquer problema encontrado nos workflows ou nos projetos Maven que impeça as etapas 1–3 de passar.
- Documentar o resultado de cada serviço (passou / falhou / motivo).

**Fora do escopo desta change:** etapas 4–9 (Docker, ECR, Terraform, Deploy, Smoke Tests, Rollback) — essas dependem de secrets AWS não configuradas.

## Capabilities

### New Capabilities

- `ci-cd/validacao-etapas-1-2-3`: Capacidade do pipeline de CI/CD de validar título de PR, compilar o projeto, executar testes e escanear dependências antes de qualquer merge em `main` — para todos os 5 microsserviços.

### Modified Capabilities

*(Nenhuma — esta é a primeira spec de CI/CD do projeto.)*

## Impact

**Arquivos de workflow afetados (leitura/correção se necessário):**
- `.github/workflows/ci-cd-api-gateway.yml`
- `.github/workflows/ci-cd-auth-service.yml`
- `.github/workflows/ci-cd-custody-service.yml`
- `.github/workflows/ci-cd-event-processor.yml`
- `.github/workflows/ci-cd-audit-service.yml`

**Possíveis causas de falha a investigar:**
- Testes de integração (`contextLoads`) que falham sem banco/Kafka disponível no runner do GitHub Actions.
- `dorny/test-reporter@v1` sem permissão de escrita no PR — pode exigir `permissions: checks: write`.
- Falta de `application-test.yml` com configurações de teste isoladas do banco real.

**Sem impacto em:** código de negócio, schema do banco, infraestrutura AWS, Docker, Terraform.
