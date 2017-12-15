variable "s3_bucket_name" {}
variable "aws_vpc_id" {}
variable "aws_utilities_subnet" {}
variable "aws_nodes_subnet" {}
variable "aws_hosted_zone_id" {}
variable "aws_access_key" {}
variable "aws_secret_key" {}


variable "aws_region" {
  default = "us-west-2"
}

variable "aws_zone" {
  default = "us-west-2c"
}
variable "aws_ssh_key" {
  default = "haystack"
}


variable "es_master_instance_type" {
  default = "r3.large.elasticsearch"
}

variable "es_worker_instance_type" {
  default = "r3.large.elasticsearch"
}

variable "es_instance_count" {
  default = 3
}
variable "es_master_count" {
  default = 3
}

variable "kafka_broker_count" {
  default = 4
}
variable "kafka_broker_instance_type" {
  default = "m4.xlarge"
}

variable "k8s_master_instance_type" {
  default = "c4.large"
}

variable "k8s_node_instance_type" {
  default = "m4.large"
}
variable "k8s_node_instance_count" {
  default = 4
}