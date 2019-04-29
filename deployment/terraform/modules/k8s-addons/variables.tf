variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}

variable "datastores_heap_memory_in_mb" {}


variable "monitoring_addons" {
  type = "map"
}

variable "alerting_addons" {
  type = "map"
}

variable "cluster" {
  type = "map"
}

variable "logging_addons" {
  type = "map"
}

variable "aa_apps_resource_limits" {
  type = "map"
}

