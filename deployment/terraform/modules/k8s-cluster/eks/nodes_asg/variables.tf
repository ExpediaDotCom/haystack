variable "cluster_name" {}
variable "aws_ssh_key" {}
variable "aws_region" {}
variable "aws_eks_cluster_endpoint" {}
variable "certificate-authority-data" {}
variable "nodes_iam-instance-profile_arn" {}
variable "app-node_instance_type" {}
variable "app-node_min_instance_count" {}
variable "app-node_max_instance_count" {}
variable "app-node_ami" {}
variable "monitoring-node_instance_type" {}
variable "monitoring-node_instance_count" {}
variable "aws_subnet_ids" {
  type = "list"
}
variable "role_name" {}

variable "nodes_security_group_ids" {
  type="list"
}
