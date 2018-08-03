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


# ========================================
# Adaptive Alerting
# ========================================

variable "alerting" {
  type = "map"
}

variable "ad-mapper" {
  type = "map"
}

variable "ad-manager" {
  type = "map"
}

variable "anomaly-validator" {
  type = "map"
}

variable "aquila-trainer" {
  type = "map"
}

# Deprecated
variable "metric-router" {
  type = "map"
}

# Deprecated
variable "ewma-detector" {
  type = "map"
}

# Deprecated
variable "constant-detector" {
  type = "map"
}

# Deprecated
variable "pewma-detector" {
  type = "map"
}
