variable "seed_node_count" {}
variable "non_seed_node_count" {}
variable "seed_node_volume_size" {}
variable "non_seed_node_volume_size" {}
variable "seed_node_instance_type" {}
variable "non_seed_node_instance_type" {}
variable "node_image" {
  default = ""
}
variable "aws_vpc_id" {}
variable "aws_subnet" {}
variable "aws_hosted_zone_id" {}
variable "aws_ssh_key_pair_name" {}
variable "graphite_host" {}
variable "graphite_port" {}
variable "haystack_cluster_name" {}
variable "haystack_cluster_role" {}