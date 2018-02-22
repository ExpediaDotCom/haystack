variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "enabled" {}
variable "node_selecter_label" {}
variable "kubewatch_config_yaml_base64" {}

variable "kubewatch_image" {
  default = "tuna/kubewatch:v0.0.1"
}
