variable "kubectl_executable_name" {}
variable "haystack_domain_name" {}
variable "k8s_app_namespace" {}
variable "traefik_node_port" {}
variable "k8s_cluster_name" {}

variable "k8s_traefik_image" {
  default = "traefik:v1.3.7"
}
variable "traefik_replicas" {
  default = "1"
}
variable "traefik_name" {
  default = "traefik-ingress-controller"
}
