variable "k8s_aws_ssh_key" {}
variable "k8s_s3_bucket_name" {}
variable "k8s_aws_region" {}
variable "k8s_aws_vpc_id" {}
variable "k8s_aws_external_master_subnet_ids" {}
variable "k8s_aws_external_worker_subnet_ids" {}
variable "k8s_node_instance_type" {}
variable "k8s_node_instance_count" {}
variable "k8s_master_instance_type" {}
variable "k8s_hosted_zone_id" {}

variable "k8s_node_ami" {
  default = "ami-06a57e7e"
}

variable "k8s_master_ami" {
  default = "ami-06a57e7e"
}

//we can't currently update the cluster name, TODO: make it configurable
variable "k8s_cluster_name" {
  default = "haystack-k8s"
}

