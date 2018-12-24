variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "container_log_path" {}
variable "es_nodes" {}
variable "k8s_storage_class" {}
variable "datastores_heap_memory_in_mb" {}

variable "es_storage_volume" {}
variable "logs_cname" {}
variable "node_selecter_label" {}

variable "enabled" {
  default = false
}
