variable "aws_ssh_key" {}
variable "s3_bucket_name" {}
variable "aws_zone" {}
variable "aws_vpc_id" {}
variable "aws_nodes_subnet_ids" {}
variable "aws_utility_subnet_ids" {}
variable "aws_hosted_zone_id" {}
variable "aws_domain_name" {}
variable "aws_region" {}

variable "node_instance_type" {}
variable "node_instance_count" {}
variable "master_instance_type" {}

variable "reverse_proxy_port" {}

variable "kops_executable_name" {}
variable "kubectl_executable_name" {}
variable "haystack_cluster_name" {}
variable "k8s_version" {
  default = "1.8.4"
}
variable "node_ami" {
  default = "ami-06a57e7e"
}

variable "master_ami" {
  default = "ami-06a57e7e"
}
