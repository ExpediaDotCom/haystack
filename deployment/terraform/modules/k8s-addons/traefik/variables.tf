variable "kubectl_executable_name" {}
variable "haystack_ui_cname" {}
variable "traefik_node_port" {}
variable "kubectl_context_name" {}

variable "k8s_traefik_image" {
  default = "traefik:v1.3.7"
}
variable "traefik_replicas" {
  default = "1"
}
variable "traefik_name" {
  default = "traefik-ingress-controller"
}
variable "app-node_selecter_label" {}