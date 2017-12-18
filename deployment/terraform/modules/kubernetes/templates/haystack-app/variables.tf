variable "app_name" {}
variable "image" {}
variable "service_port" {
  default = 80
}
variable "container_port" {
  default = 8080
}
variable "replicas" {}

variable "create_service" {
  default = false
}
variable "enabled" {
  default = true
}
variable "termination_grace_period" {
  default = 30
}
variable "namespace" {
  default = "haystack-apps"
}
