variable "kubectl_executable_name" {}

variable "k8s_minikube_cluster_name" {
  default = "minikube"
}
variable "k8s_app_name_space" {
  default = "haystack-apps"
}

variable "haystack_domain_name" {
  default = "haystack.local"
}
variable "traefik_node_port" {
  default = "32300"
}
