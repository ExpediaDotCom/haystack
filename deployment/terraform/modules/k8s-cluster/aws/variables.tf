variable "graphite_node_port" {}
variable "kops_executable_name" {}
variable "kubectl_executable_name" {}


variable "kops_kubernetes" {
  type = "map"
}
variable "cluster" {
  type = "map"
}
