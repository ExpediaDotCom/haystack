variable "elasticsearch_hostname" {}
variable "elasticsearch_port" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "cassandra_hostname" {}
variable "cassandra_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "haystack_cluster_name" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "k8s_app_namespace" {}
variable "app-node_selector_label"{}

# traces config
variable "traces" {
  type = "map"
}

# trends config
variable "trends" {
  type = "map"
}

# pipes config
variable "pipes" {
  type = "map"
}

# collectors config
variable "collector" {
  type = "map"
}

# service-graph config
variable "service-graph" {
  type = "map"
}

# ui config
variable "ui" {
  type = "map"
}

# metrictank
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

# Deprecated - to be replaced by ad-mapper above
variable "metric-router" {
  type = "map"
}

# Deprecated - to be replaced by ad-manager above
variable "ewma-detector" {
  type = "map"
}

# Deprecated - to be replaced by ad-manager above
variable "constant-detector" {
  type = "map"
}

# Deprecated - to be replaced by ad-manager above
variable "pewma-detector" {
  type = "map"
}
