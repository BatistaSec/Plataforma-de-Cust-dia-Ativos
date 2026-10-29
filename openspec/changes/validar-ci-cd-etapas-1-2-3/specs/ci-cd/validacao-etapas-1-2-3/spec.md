# Spec Delta

## Purpose

Define o comportamento esperado das etapas 1 (Validate Branch), 2 (Build + Tests) e 3 (Security & Quality) do pipeline de CI/CD para os 5 microsserviços da plataforma, garantindo que todo código mergeado em `main` passou por validação de título, compilação, testes e varredura de segurança.

## ADDED Requirements

### Requirement: Título do PR deve seguir Conventional Commits
O pipeline DEVE rejeitar PRs cujo título não siga o padrão Conventional Commits: `^(feat|fix|chore|ci|docs|refactor|test|perf|revert)(\(.+\))?!?:.+`

#### Scenario: Título válido passa a validação
- **GIVEN** um PR aberto com título `feat(auth): adicionar refresh token`
- **WHEN** o job `1 · Validate Branch` executa
- **THEN** o job conclui com ✅ sucesso

#### Scenario: Título inválido bloqueia o pipeline
- **GIVEN** um PR aberto com título `corrigindo bug no login`
- **WHEN** o job `1 · Validate Branch` executa
- **THEN** o job falha com ❌ e exibe mensagem indicando o formato esperado

#### Scenario: Push direto em main não bloqueia (apenas avisa)
- **GIVEN** um push direto na branch `main`
- **WHEN** o job `1 · Validate Branch` executa no auth-service
- **THEN** o job exibe mensagem informativa e conclui com ✅ (proteção real deve ser via branch protection rules)

### Requirement: Build Maven deve compilar sem erros para todos os serviços
O pipeline DEVE compilar cada microsserviço com `mvn -pl <servico> -am clean package -DskipTests -B` e armazenar o JAR como artefato.

#### Scenario: Build bem-sucedido gera artefato
- **GIVEN** código Java válido no módulo do serviço
- **WHEN** o job `2 · Build` executa
- **THEN** o job conclui com ✅ e o JAR é carregado como artefato GitHub Actions com retenção de 1 dia

#### Scenario: Erro de compilação bloqueia pipeline
- **GIVEN** código Java com erro de compilação
- **WHEN** o job `2 · Build` executa
- **THEN** o job falha com ❌ e os jobs dependentes (test, security, docker) não executam

### Requirement: Testes devem executar e publicar relatório
O pipeline DEVE executar `mvn -pl <servico> -am verify -B` e publicar o relatório JUnit via `dorny/test-reporter@v1`.

#### Scenario: Testes passando publicam relatório verde
- **GIVEN** todos os testes unitários passam
- **WHEN** o job `2 · Tests` executa
- **THEN** o job conclui com ✅ e o relatório JUnit aparece na aba Checks do PR

#### Scenario: Testes falhando bloqueiam o pipeline
- **GIVEN** pelo menos um teste JUnit falha
- **WHEN** o job `2 · Tests` executa
- **THEN** o job falha com ❌ e o relatório mostra os testes com falha

#### Scenario: Timeout protege contra testes travados
- **GIVEN** um teste que trava indefinidamente (ex: contextLoads sem banco)
- **WHEN** o job `2 · Tests` executa
- **THEN** o job é cancelado automaticamente após o timeout configurado (15 min para api-gateway/auth-service/custody-service, 20 min para event-processor/audit-service)

### Requirement: Gitleaks não deve encontrar segredos no código
O pipeline DEVE varrer o histórico completo do repositório com `gitleaks/gitleaks-action@v2` e bloquear se encontrar segredos.

#### Scenario: Código sem segredos passa a varredura
- **GIVEN** nenhum token, senha ou chave privada exposta no código ou histórico
- **WHEN** o job `3 · Security & Quality` executa o step Gitleaks
- **THEN** o step conclui com ✅

#### Scenario: Segredo no código bloqueia o pipeline
- **GIVEN** uma string que parece um token AWS (ex: `AKIAIOSFODNN7EXAMPLE`) commitada no código
- **WHEN** o job `3 · Security & Quality` executa o step Gitleaks
- **THEN** o step falha com ❌ e exibe o arquivo e linha onde o segredo foi detectado

### Requirement: Trivy não deve encontrar vulnerabilidades CRITICAL ou HIGH nas dependências
O pipeline DEVE varrer as dependências do módulo com `aquasecurity/trivy-action@0.24.0` (scan-type fs) e bloquear para vulnerabilidades CRITICAL ou HIGH com fix disponível.

#### Scenario: Dependências sem vulnerabilidades críticas passam
- **GIVEN** todas as dependências Maven estão sem vulnerabilidades CRITICAL/HIGH conhecidas com fix disponível
- **WHEN** o job `3 · Security & Quality` executa o step Trivy
- **THEN** o step conclui com ✅

#### Scenario: Vulnerabilidade CRITICAL bloqueia o pipeline
- **GIVEN** uma dependência com vulnerabilidade CRITICAL com fix disponível
- **WHEN** o job `3 · Security & Quality` executa o step Trivy
- **THEN** o step falha com ❌ (exit-code 1) e exibe a CVE, a versão afetada e a versão com fix

#### Scenario: Vulnerabilidade sem fix disponível não bloqueia
- **GIVEN** uma dependência com vulnerabilidade HIGH mas sem patch disponível
- **WHEN** o job `3 · Security & Quality` executa o step Trivy
- **THEN** o step conclui com ✅ (ignore-unfixed: true)

### Requirement: Etapas 4–9 não executam em PRs
O pipeline DEVE executar somente as etapas 1–3 em PRs, reservando Docker, ECR, Terraform e Deploy exclusivamente para push em `main`.

#### Scenario: PR não aciona deploy
- **GIVEN** um PR aberto para a branch main
- **WHEN** o pipeline executa
- **THEN** apenas os jobs validate-branch, build, test e security executam; os jobs docker, terraform, deploy, smoke e rollback são skipped
