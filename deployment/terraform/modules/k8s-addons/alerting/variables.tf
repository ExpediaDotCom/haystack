variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}

variable "cluster" {
  type = "map"
}

variable "alerting_addons" {
  type = "map"
}
