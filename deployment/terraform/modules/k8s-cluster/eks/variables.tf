variable "graphite_node_port" {}
variable "aws_iam_authenticator_executable_name" {}
variable "kubectl_executable_name" {}
variable "role_name" {}

variable "eks_kubernetes" {
  type = "map"
}
variable "cluster" {
  type = "map"
}

