variable "storage_backend_image" {}
variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}
variable "elasticsearch_hostname" {}
variable "elasticsearch_port" {}
variable "elasticsearch_template" {}
variable "cassandra_hostname" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "enabled"{}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selector_label"{}
variable "memory_request"{}
variable "memory_limit"{}
variable "jvm_memory_limit"{}
variable "cpu_request"{}
variable "cpu_limit"{}
variable "backend_memory_request"{}
variable "backend_memory_limit"{}
variable "backend_jvm_memory_limit"{}
variable "backend_cpu_request"{}
variable "backend_cpu_limit"{}
variable "env_vars" {}
variable "backend_env_vars" {}
variable "enable_kafka_sink" {
  default = false
}

variable "termination_grace_period" {
  default = 30
}
