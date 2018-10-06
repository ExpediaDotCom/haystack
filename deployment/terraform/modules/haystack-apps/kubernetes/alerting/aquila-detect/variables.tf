# Docker
variable "image" {}
variable "image_pull_policy" {
  default = "IfNotPresent"
}

# Kubernetes
variable "namespace" {}
variable "enabled" {}
variable "replicas" {}
variable "cpu_limit" {}
variable "cpu_request" {}
variable "memory_limit" {}
variable "memory_request" {}
variable "node_selector_label" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "service_port" {
  default = 80
}
variable "container_port" {
  default = 8080
}

# Environment
variable "jvm_memory_limit" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "env_vars" {}
