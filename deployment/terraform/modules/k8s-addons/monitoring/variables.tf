variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "datastores_heap_memory_in_mb" {}
variable "monitoring_addons" {
  type = "map"
}

variable "cluster" {
  type = "map"
}
