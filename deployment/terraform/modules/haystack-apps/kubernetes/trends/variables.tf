
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "cassandra_hostname" {}
variable "cassandra_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "app_namespace" {}
variable "node_selector_label"{}

variable "default_memory_limit"{}
variable "default_cpu_limit"{}

# trends config
variable "trends" {
  type = "map"
}


#metrictank
variable "metrictank" {
  type = "map"
}

