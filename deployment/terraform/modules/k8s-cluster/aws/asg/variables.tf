variable "k8s_aws_ssh_key" {}
variable "k8s_s3_bucket_name" {}
variable "k8s_aws_zone" {}
variable "k8s_aws_nodes_subnet_ids" {}
variable "k8s_node_instance_type" {}
variable "k8s_node_instance_count" {}
variable "k8s_master_instance_type" {}
variable "k8s_aws_region" {}
variable "k8s_cluster_name" {}
variable "master_iam-instance-profile_arn" {}
variable "master_security_groups" {}
variable "nodes_iam-instance-profile_arn" {}
variable "node_security_groups" {}


variable "k8s_node_ami" {
  default = "ami-06a57e7e"
}

variable "k8s_master_ami" {
  default = "ami-06a57e7e"
}
