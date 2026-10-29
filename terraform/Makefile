ENV ?= dev

init:
	terraform init

plan:
	terraform plan -var="environment=$(ENV)" -out=tfplan

apply:
	terraform apply -auto-approve tfplan

destroy:
	terraform destroy -var="environment=$(ENV)"

fmt:
	terraform fmt -recursive

validate:
	terraform validate

# Criar bucket S3 e tabela DynamoDB para o estado remoto (executar uma vez)
bootstrap:
	aws s3api create-bucket \
		--bucket custody-portfolio-tfstate \
		--region us-east-1
	aws s3api put-bucket-versioning \
		--bucket custody-portfolio-tfstate \
		--versioning-configuration Status=Enabled
	aws s3api put-bucket-encryption \
		--bucket custody-portfolio-tfstate \
		--server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
	aws dynamodb create-table \
		--table-name custody-portfolio-tfstate-lock \
		--attribute-definitions AttributeName=LockID,AttributeType=S \
		--key-schema AttributeName=LockID,KeyType=HASH \
		--billing-mode PAY_PER_REQUEST \
		--region us-east-1
	@echo "Bootstrap concluído. Agora rode: make init"
