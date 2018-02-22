variable "kubectl_executable_name" {}
variable "k8s_minikube_cluster_name" {
  default = "minikube"
}

variable "kubewatch_config_yaml_base64" {
  default = ""
}

variable "haystack_cluster_name" {
  default = "haystack"
}
variable "haystack_domain_name" {
  default = "local"
}
variable "reverse_proxy_port" {
  default = "32300"
}

variable "graphite_node_port" {
  default = "32301"
}
