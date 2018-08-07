variable "kubectl_context_name" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "kubectl_executable_name" {}
variable "app_namespace" {}
variable "node_selector_label"{}

variable "metrictank" {
  type = "map"
}

variable "alerting" {
  type = "map"
}

variable "metric-router" {
  type = "map"
}

variable "ewma-detector" {
  type = "map"
}

variable "constant-detector" {
  type = "map"
}

variable "pewma-detector" {
  type = "map"
}

variable "anomaly-validator" {
  type = "map"
}

variable "ad-mapper" {
  type = "map"
}

variable "ad-manager" {
  type = "map"
}

variable "modelservice" {
  type = "map"
}

variable "aquila-trainer" {
  type = "map"
}
