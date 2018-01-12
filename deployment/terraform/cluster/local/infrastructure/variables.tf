variable "kubectl_executable_name" {}
variable "k8s_minikube_cluster_name" {
  default = "minikube"
}
variable "haystack_domain_name" {
  default = "haystack.local"
}
variable "reverse_proxy_port" {
  default = "32300"
}
