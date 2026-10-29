variable "project"               { type = string }
variable "environment"           { type = string }
variable "service_name"          { type = string }
variable "cluster_arn"           { type = string }
variable "task_execution_role_arn" { type = string }
variable "container_image"       { type = string }
variable "container_port"        { type = number }
variable "cpu"                   { type = number; default = 256 }
variable "memory"                { type = number; default = 512 }
variable "desired_count"         { type = number; default = 1 }
variable "vpc_id"                { type = string }
variable "private_subnet_ids"    { type = list(string) }
variable "target_group_arn"      { type = string; default = "" }
variable "log_group_name"        { type = string }
variable "aws_region"            { type = string }

variable "environment_variables" {
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

variable "secrets" {
  type = list(object({
    name      = string
    valueFrom = string
  }))
  default = []
}

variable "allowed_inbound_sg_ids" {
  type    = list(string)
  default = []
}
