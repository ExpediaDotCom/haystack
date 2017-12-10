variable "k8s_aws_ssh_key" {
  default = "haystack"
}

variable "ks8_cluster_name" {
  default = "haystack-k8s"
}
variable "k8s_s3_bucket_name" {
  default = "haystack-deployment-tf"
}

variable "k8s_aws_region" {}

variable "k8s_aws_vpc_id" {}

variable "k8s_aws_external_master_subnet_ids" {
}

variable "k8s_aws_external_worker_subnet_ids" {
}

variable "k8s_base_domain_name" {}

