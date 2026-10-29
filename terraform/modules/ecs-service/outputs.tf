output "service_name"       { value = aws_ecs_service.main.name }
output "security_group_id"  { value = aws_security_group.service.id }
output "task_definition_arn" { value = aws_ecs_task_definition.main.arn }
