variable "worker_instance_type" {}
variable "master_instance_type" {}
variable "worker_instance_count" {}
variable "master_instance_count" {}
variable "k8s_nodes_iam-role_arn" {}
variable "haystack_cluster_name" {}
variable "aws_vpc_id" {}
variable "aws_subnet" {}
variable "aws_region" {}

variable "haystack_index_store_es_version" {
  default = "6.0"
}

