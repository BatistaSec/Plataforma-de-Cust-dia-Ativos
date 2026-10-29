output "vpc_id" {
  description = "ID da VPC"
  value       = module.vpc.vpc_id
}

output "rds_endpoint" {
  description = "Endpoint do banco de dados RDS"
  value       = module.rds.endpoint
}

output "msk_bootstrap_brokers" {
  description = "Bootstrap brokers do MSK (Kafka)"
  value       = module.msk.bootstrap_brokers
}

output "elasticache_endpoint" {
  description = "Endpoint do ElastiCache (Redis)"
  value       = module.elasticache.endpoint
}

output "ecs_cluster_name" {
  description = "Nome do cluster ECS"
  value       = module.ecs_cluster.cluster_name
}

output "alb_dns_name" {
  description = "DNS do Application Load Balancer"
  value       = module.alb.dns_name
}
