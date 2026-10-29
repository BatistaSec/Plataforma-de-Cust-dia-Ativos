# GitHub Secrets — Plataforma de Custódia de Ativos

Configure em: **Settings → Secrets and variables → Actions**

---

## Secrets obrigatórios para CI/CD

### AWS

| Secret | Descrição |
|---|---|
| `AWS_ACCESS_KEY_ID` | Access key do usuário IAM (ver permissões abaixo) |
| `AWS_SECRET_ACCESS_KEY` | Secret key correspondente |

### URLs dos serviços (Smoke Tests)

| Secret | Valor esperado |
|---|---|
| `API_GATEWAY_URL` | `https://api.seudominio.com` |
| `AUTH_SERVICE_URL` | URL interna ou pública do auth-service |
| `CUSTODY_SERVICE_URL` | URL interna ou pública do custody-service |
| `EVENT_PROCESSOR_URL` | URL interna ou pública do event-processor |
| `AUDIT_SERVICE_URL` | URL interna ou pública do audit-service |

### Token para smoke tests autenticados

| Secret | Descrição |
|---|---|
| `SMOKE_TEST_TOKEN` | JWT válido para usar nos smoke tests que exigem autenticação |

---

## Branch Protection Rules recomendadas

Configure em: **Settings → Branches → Add branch protection rule** (padrão: `main`)

```
✅ Require a pull request before merging
   ✅ Require approvals: 1
   ✅ Dismiss stale pull request approvals when new commits are pushed

✅ Require status checks to pass before merging
   Status checks obrigatórios (adicionar após o primeiro run):
   - 1 · Validate Branch
   - 2 · Build
   - 2 · Tests
   - 3 · Security & Quality

✅ Require branches to be up to date before merging
✅ Do not allow bypassing the above settings
```

---

## Permissões IAM mínimas necessárias

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ECRAccess",
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:DescribeRepositories",
        "ecr:ListImages"
      ],
      "Resource": "*"
    },
    {
      "Sid": "ECSAccess",
      "Effect": "Allow",
      "Action": [
        "ecs:DescribeTaskDefinition",
        "ecs:RegisterTaskDefinition",
        "ecs:UpdateService",
        "ecs:DescribeServices",
        "ecs:ListTasks",
        "ecs:DescribeTasks"
      ],
      "Resource": "*"
    },
    {
      "Sid": "ECSWaiter",
      "Effect": "Allow",
      "Action": [
        "ecs:ListServices",
        "ecs:DescribeClusters"
      ],
      "Resource": "*"
    },
    {
      "Sid": "IAMPassRole",
      "Effect": "Allow",
      "Action": [
        "iam:PassRole"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## Resumo do pipeline por microsserviço

| Etapa | Push em main | PR para main |
|---|---|---|
| 1 · Validate Branch (PR title) | ✅ | ✅ |
| 2 · Build | ✅ | ✅ |
| 2 · Tests | ✅ | ✅ |
| 3 · Security & Quality (Gitleaks + Trivy fs) | ✅ | ✅ |
| 4 · Docker Build + Trivy image scan | ✅ | ❌ |
| 5 · Push ECR (SHA + latest) | ✅ | ❌ |
| 6 · Terraform Plan & Apply | ✅ | ❌ |
| 7 · Deploy ECS Fargate | ✅ | ❌ |
| 8 · Smoke Tests | ✅ | ❌ |
| 9 · Rollback automático | ✅ (se smoke falhar) | ❌ |
