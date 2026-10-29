variable "project"           { type = string }
variable "environment"       { type = string }
variable "vpc_id"            { type = string }
variable "public_subnet_ids" { type = list(string) }
variable "services" {
  type = map(object({
    port        = number
    health_path = string
  }))
  description = "Mapa de serviços para criar target groups"
}
