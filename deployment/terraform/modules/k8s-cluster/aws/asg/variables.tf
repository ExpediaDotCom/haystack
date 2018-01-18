variable "aws_ssh_key" {}
variable "s3_bucket_name" {}
variable "aws_zone" {}
variable "aws_nodes_subnet_ids" {}
variable "nodes_instance_type" {}
variable "nodes_instance_count" {}
variable "masters_instance_type" {}
variable "k8s_cluster_name" {}
variable "masters_iam-instance-profile_arn" {}
variable "haystack_cluster_name" {}
variable "nodes_iam-instance-profile_arn" {}

variable "masters_security_groups" {
  type = "list"
}
variable "nodes_security_groups" {
  type = "list"
}
variable "nodes_ami" {
  default = "ami-06a57e7e"
}
variable "masters_ami" {
  default = "ami-06a57e7e"
}
