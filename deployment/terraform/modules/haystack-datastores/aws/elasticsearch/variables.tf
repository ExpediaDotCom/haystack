variable "haystack_index_store_worker_instance_type" {}
variable "haystack_index_store_master_instance_type" {}
variable "haystack_index_store_worker_instance_count" {}
variable "haystack_index_store_master_instance_count" {}
variable "haystack_index_store_es_version" {
  default = "6.0"
}
variable "haystack_cluster_name" {}
variable "aws_vpc_id" {}
variable "aws_subnet" {}


