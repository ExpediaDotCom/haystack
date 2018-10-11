variable "image" {}
variable "image_pull_policy" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}
variable "webhook_url" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}

variable "enabled" {}

variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selector_label" {}
variable "memory_limit" {}
variable "memory_request" {}
variable "jvm_memory_limit" {}
variable "cpu_limit" {}
variable "cpu_request" {}
variable "env_vars" {}
variable "termination_grace_period" {
  default = 30
}
variable "service_port" {
  default = 80
}
variable "container_port" {
  default = 8080
}
