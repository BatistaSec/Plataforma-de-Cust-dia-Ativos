terraform {
  required_version = ">= 1.7.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
  }

  # Estado remoto no S3 — crie o bucket antes de rodar terraform init
  backend "s3" {
    bucket         = "custody-portfolio-tfstate"
    key            = "global/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "custody-portfolio-tfstate-lock"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "custody-portfolio"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
