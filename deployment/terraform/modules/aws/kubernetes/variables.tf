variable "k8s_aws_ssh_key" {}
variable "k8s_s3_bucket_name" {}
variable "k8s_aws_zone" {}
variable "k8s_aws_vpc_id" {}
variable "k8s_aws_nodes_subnet_ids" {}
variable "k8s_aws_utility_subnet_ids" {}
variable "k8s_node_instance_type" {}
variable "k8s_node_instance_count" {}
variable "k8s_master_instance_type" {}
variable "k8s_hosted_zone_id" {}
variable "k8s_base_domain_name" {}

variable "k8s_logs_es_url" {}
variable "k8s_aws_region" {}


variable "kubectl_executable_name" {
  default = "kubectl"
}
variable "kops_executable_name" {
  default = "kops"
}
variable "k8s_version" {
  default = "1.8.4"
}
variable "k8s_node_ami" {
  default = "ami-06a57e7e"
}

variable "k8s_master_ami" {
  default = "ami-06a57e7e"
}
