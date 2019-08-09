variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "namespace" {}
variable "node_selector_label"{}

variable "reverse-proxy" {
  type = "map"
}

variable "service_port" {
  default = 34002
}