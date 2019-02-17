variable "aws_ssh_key" {}
variable "s3_bucket_name" {}
variable "aws_zone" {}
variable "aws_nodes_subnet" {}
variable "app-nodes_instance_type" {}
variable "app-nodes_instance_count" {}
variable "monitoring-nodes_instance_type" {}
variable "monitoring-nodes_instance_count" {}
variable "masters_instance_type" {}
variable "k8s_cluster_name" {}
variable "masters_iam-instance-profile_arn" {}
variable "haystack_cluster_name" {}
variable "haystack_cluster_role" {}
variable "haystack_cluster_env" {}
variable "nodes_iam-instance-profile_arn" {}

variable "monitoring-nodes_instance_volume" {}
variable "master_instance_volume" {}
variable "app-nodes_instance_volume" {}
variable "nodes_ami" {}
variable "masters_ami" {}

variable "masters_security_groups" {
  type = "list"
}
variable "nodes_security_groups" {
  type = "list"
}
