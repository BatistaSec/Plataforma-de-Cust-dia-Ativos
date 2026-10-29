# ─── VPC ──────────────────────────────────────────────────────────────────────
module "vpc" {
  source      = "./modules/vpc"
  cidr        = var.vpc_cidr
  project     = var.project
  environment = var.environment
}

# ─── ECS Cluster ──────────────────────────────────────────────────────────────
module "ecs_cluster" {
  source      = "./modules/ecs-cluster"
  project     = var.project
  environment = var.environment
}

# ─── ALB ──────────────────────────────────────────────────────────────────────
module "alb" {
  source            = "./modules/alb"
  project           = var.project
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  services = {
    "api-gateway"     = { port = 8080, health_path = "/actuator/health" }
    "auth-service"    = { port = 8081, health_path = "/actuator/health" }
    "custody-service" = { port = 8082, health_path = "/actuator/health" }
    "event-processor" = { port = 8083, health_path = "/actuator/health" }
    "audit-service"   = { port = 8084, health_path = "/actuator/health" }
  }
}

# ─── RDS PostgreSQL ────────────────────────────────────────────────────────────
module "rds" {
  source             = "./modules/rds"
  project            = var.project
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  db_username        = var.db_username
  db_password        = var.db_password
  db_name            = var.db_name
  allowed_sg_ids = [
    module.auth_service.security_group_id,
    module.custody_service.security_group_id,
    module.event_processor.security_group_id,
    module.audit_service.security_group_id,
  ]
}

# ─── MSK (Kafka) ───────────────────────────────────────────────────────────────
module "msk" {
  source             = "./modules/msk"
  project            = var.project
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  broker_count       = 2
  allowed_sg_ids = [
    module.custody_service.security_group_id,
    module.event_processor.security_group_id,
    module.audit_service.security_group_id,
  ]
}

# ─── ElastiCache (Redis) ────────────────────────────────────────────────────────
module "elasticache" {
  source             = "./modules/elasticache"
  project            = var.project
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  allowed_sg_ids     = [module.custody_service.security_group_id]
}

# ─── ECR Repositories ──────────────────────────────────────────────────────────
module "ecr_api_gateway"     { source = "./modules/ecr"; repository_name = "api-gateway";     project = var.project; environment = var.environment }
module "ecr_auth_service"    { source = "./modules/ecr"; repository_name = "auth-service";    project = var.project; environment = var.environment }
module "ecr_custody_service" { source = "./modules/ecr"; repository_name = "custody-service"; project = var.project; environment = var.environment }
module "ecr_event_processor" { source = "./modules/ecr"; repository_name = "event-processor"; project = var.project; environment = var.environment }
module "ecr_audit_service"   { source = "./modules/ecr"; repository_name = "audit-service";   project = var.project; environment = var.environment }

# ─── ECS Services ──────────────────────────────────────────────────────────────
module "api_gateway" {
  source                  = "./modules/ecs-service"
  project                 = var.project
  environment             = var.environment
  service_name            = "api-gateway"
  cluster_arn             = module.ecs_cluster.cluster_arn
  task_execution_role_arn = module.ecs_cluster.task_execution_role_arn
  container_image         = "${module.ecr_api_gateway.repository_url}:latest"
  container_port          = 8080
  vpc_id                  = module.vpc.vpc_id
  private_subnet_ids      = module.vpc.private_subnet_ids
  log_group_name          = module.ecs_cluster.log_group_name
  aws_region              = var.aws_region
  target_group_arn        = module.alb.target_group_arns["api-gateway"]
  allowed_inbound_sg_ids  = [module.alb.security_group_id]
  environment_variables = [
    { name = "SPRING_PROFILES_ACTIVE", value = var.environment },
    { name = "AUTH_SERVICE_URL",       value = "http://${module.auth_service.service_name}:8081" },
    { name = "CUSTODY_SERVICE_URL",    value = "http://${module.custody_service.service_name}:8082" },
  ]
}

module "auth_service" {
  source                  = "./modules/ecs-service"
  project                 = var.project
  environment             = var.environment
  service_name            = "auth-service"
  cluster_arn             = module.ecs_cluster.cluster_arn
  task_execution_role_arn = module.ecs_cluster.task_execution_role_arn
  container_image         = "${module.ecr_auth_service.repository_url}:latest"
  container_port          = 8081
  vpc_id                  = module.vpc.vpc_id
  private_subnet_ids      = module.vpc.private_subnet_ids
  log_group_name          = module.ecs_cluster.log_group_name
  aws_region              = var.aws_region
  target_group_arn        = module.alb.target_group_arns["auth-service"]
  allowed_inbound_sg_ids  = [module.alb.security_group_id, module.api_gateway.security_group_id]
  environment_variables = [
    { name = "SPRING_PROFILES_ACTIVE",   value = var.environment },
    { name = "SPRING_DATASOURCE_URL",    value = module.rds.connection_url },
    { name = "SPRING_DATASOURCE_USERNAME", value = var.db_username },
  ]
  secrets = [
    { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "arn:aws:secretsmanager:${var.aws_region}:*:secret:${var.project}/db-password" },
    { name = "JWT_SECRET",                 valueFrom = "arn:aws:secretsmanager:${var.aws_region}:*:secret:${var.project}/jwt-secret" },
  ]
}

module "custody_service" {
  source                  = "./modules/ecs-service"
  project                 = var.project
  environment             = var.environment
  service_name            = "custody-service"
  cluster_arn             = module.ecs_cluster.cluster_arn
  task_execution_role_arn = module.ecs_cluster.task_execution_role_arn
  container_image         = "${module.ecr_custody_service.repository_url}:latest"
  container_port          = 8082
  vpc_id                  = module.vpc.vpc_id
  private_subnet_ids      = module.vpc.private_subnet_ids
  log_group_name          = module.ecs_cluster.log_group_name
  aws_region              = var.aws_region
  target_group_arn        = module.alb.target_group_arns["custody-service"]
  allowed_inbound_sg_ids  = [module.alb.security_group_id, module.api_gateway.security_group_id]
  environment_variables = [
    { name = "SPRING_PROFILES_ACTIVE",      value = var.environment },
    { name = "SPRING_DATASOURCE_URL",       value = module.rds.connection_url },
    { name = "SPRING_DATASOURCE_USERNAME",  value = var.db_username },
    { name = "SPRING_KAFKA_BOOTSTRAP_SERVERS", value = module.msk.bootstrap_brokers },
    { name = "SPRING_DATA_REDIS_HOST",      value = module.elasticache.endpoint },
    { name = "SPRING_DATA_REDIS_PORT",      value = tostring(module.elasticache.port) },
  ]
  secrets = [
    { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "arn:aws:secretsmanager:${var.aws_region}:*:secret:${var.project}/db-password" },
  ]
}

module "event_processor" {
  source                  = "./modules/ecs-service"
  project                 = var.project
  environment             = var.environment
  service_name            = "event-processor"
  cluster_arn             = module.ecs_cluster.cluster_arn
  task_execution_role_arn = module.ecs_cluster.task_execution_role_arn
  container_image         = "${module.ecr_event_processor.repository_url}:latest"
  container_port          = 8083
  vpc_id                  = module.vpc.vpc_id
  private_subnet_ids      = module.vpc.private_subnet_ids
  log_group_name          = module.ecs_cluster.log_group_name
  aws_region              = var.aws_region
  target_group_arn        = module.alb.target_group_arns["event-processor"]
  allowed_inbound_sg_ids  = [module.alb.security_group_id]
  environment_variables = [
    { name = "SPRING_PROFILES_ACTIVE",           value = var.environment },
    { name = "SPRING_DATASOURCE_URL",            value = module.rds.connection_url },
    { name = "SPRING_DATASOURCE_USERNAME",       value = var.db_username },
    { name = "SPRING_KAFKA_BOOTSTRAP_SERVERS",   value = module.msk.bootstrap_brokers },
  ]
  secrets = [
    { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "arn:aws:secretsmanager:${var.aws_region}:*:secret:${var.project}/db-password" },
  ]
}

module "audit_service" {
  source                  = "./modules/ecs-service"
  project                 = var.project
  environment             = var.environment
  service_name            = "audit-service"
  cluster_arn             = module.ecs_cluster.cluster_arn
  task_execution_role_arn = module.ecs_cluster.task_execution_role_arn
  container_image         = "${module.ecr_audit_service.repository_url}:latest"
  container_port          = 8084
  vpc_id                  = module.vpc.vpc_id
  private_subnet_ids      = module.vpc.private_subnet_ids
  log_group_name          = module.ecs_cluster.log_group_name
  aws_region              = var.aws_region
  target_group_arn        = module.alb.target_group_arns["audit-service"]
  allowed_inbound_sg_ids  = [module.alb.security_group_id]
  environment_variables = [
    { name = "SPRING_PROFILES_ACTIVE",           value = var.environment },
    { name = "SPRING_DATASOURCE_URL",            value = module.rds.connection_url },
    { name = "SPRING_DATASOURCE_USERNAME",       value = var.db_username },
    { name = "SPRING_KAFKA_BOOTSTRAP_SERVERS",   value = module.msk.bootstrap_brokers },
  ]
  secrets = [
    { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "arn:aws:secretsmanager:${var.aws_region}:*:secret:${var.project}/db-password" },
  ]
}
