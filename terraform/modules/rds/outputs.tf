output "endpoint"          { value = aws_db_instance.main.address }
output "port"              { value = aws_db_instance.main.port }
output "security_group_id" { value = aws_security_group.rds.id }
output "connection_url" {
  value     = "jdbc:postgresql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/${var.db_name}"
  sensitive = false
}
