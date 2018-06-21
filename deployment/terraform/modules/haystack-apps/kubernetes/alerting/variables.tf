variable "kubectl_context_name" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "kubectl_executable_name" {}
variable "app_namespace" {}
variable "node_selector_label"{}


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

