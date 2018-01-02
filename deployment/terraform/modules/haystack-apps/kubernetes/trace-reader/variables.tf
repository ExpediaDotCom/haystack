variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "enabled"{}

variable "termination_grace_period" {
  default = 30
}

variable "service_port" {
  default = 8080
}
variable "container_port" {
  default = 8080
}