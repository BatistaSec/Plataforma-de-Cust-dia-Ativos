output "cluster_name"             { value = aws_ecs_cluster.main.name }
output "cluster_arn"              { value = aws_ecs_cluster.main.arn }
output "task_execution_role_arn"  { value = aws_iam_role.ecs_task_execution.arn }
output "log_group_name"           { value = aws_cloudwatch_log_group.ecs.name }
