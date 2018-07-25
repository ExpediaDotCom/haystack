variable "kubectl_context_name" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "kubectl_executable_name" {}
variable "app_namespace" {}
variable "node_selector_label"{}

#metrictank
variable "metrictank" {
  type = "map"
}

# alerting config
variable "alerting" {
  type = "map"
}
# metric-router config
variable "metric-router" {
  type = "map"
}

# ewma-detector config
variable "ewma-detector" {
  type = "map"
}

#constant-detector
variable "constant-detector" {
  type = "map"
}
#pewma-detector
variable "pewma-detector" {
  type = "map"
}

#anomaly-validator
variable "anomaly-validator" {
  type = "map"
}

#anomaly-detector-mapper
variable "anomaly-detector-mapper" {
  type = "map"
}

#anomaly-detector-manager
variable "anomaly-detector-manager" {
  type = "map"
}
