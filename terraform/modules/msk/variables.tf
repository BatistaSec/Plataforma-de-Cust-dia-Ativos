variable "project"            { type = string }
variable "environment"        { type = string }
variable "vpc_id"             { type = string }
variable "private_subnet_ids" { type = list(string) }
variable "allowed_sg_ids"     { type = list(string); default = [] }
variable "kafka_version"      { type = string; default = "3.6.0" }
variable "broker_count"       { type = number; default = 2 }
