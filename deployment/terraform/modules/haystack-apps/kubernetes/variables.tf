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
variable "aa_app_namespace" {}
variable "app-node_selector_label" {}
variable "domain_name" {}
variable "kinesis-stream_name" {}
variable "kinesis-stream_region" {}
variable "pipes_firehose_writer_firehose_streamname" {}

# ========================================
# Haystack
# ========================================

variable "traces" {
  type = "map"
}

variable "trends" {
  type = "map"
}

variable "pipes" {
  type = "map"
}

variable "collector" {
  type = "map"
}

variable "service-graph" {
  type = "map"
}

variable "ui" {
  type = "map"
}

variable "metrictank" {
  type = "map"
}

variable "haystack-alerts" {
  type = "map"
}


# ========================================
# Adaptive Alerting
# ========================================

variable "alerting" {
  type = "map"
}

variable "modelservice" {
  type = "map"
}

variable "ad-mapper" {
  type = "map"
}

variable "ad-manager" {
  type = "map"
}

variable "mc-a2m-mapper" {
  type = "map"
}

variable "notifier" {
  type = "map"
}


# ========================================
# Alert Manager
# ========================================

variable "alert-manager-service" {
  type = "map"
}

variable "alert-manager-notifier" {
  type = "map"
}

variable "alert-manager-store" {
  type = "map"
}

# ========================================
# Pitchfork
# ========================================
variable "pitchfork" {
  type = "map"
}

# ========================================
# Blobs HTTP Reverse Proxy
# ========================================
variable "reverse-proxy" {
  type = "map"
}

# ========================================
# Haystack Agent
# ========================================
variable "haystack-agent" {
  type = "map"
}