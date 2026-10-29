variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "project" {
  description = "Nome do projeto"
  type        = string
  default     = "custody-portfolio"
}

variable "vpc_cidr" {
  description = "CIDR da VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_password" {
  description = "Senha do banco de dados PostgreSQL"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "Usuário do banco de dados"
  type        = string
  default     = "custody_user"
}

variable "db_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "custody_db"
}

variable "jwt_secret" {
  description = "Secret para assinatura do JWT"
  type        = string
  sensitive   = true
}
