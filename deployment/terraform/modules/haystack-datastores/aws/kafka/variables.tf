variable "broker_count" {}
variable "broker_volume_size" {}
variable "broker_instance_type" {}
variable "broker_image" {
  default = ""
}
variable "aws_vpc_id" {}
variable "aws_subnet" {}
variable "aws_hosted_zone_id" {}
variable "aws_ssh_key_pair_name" {}
variable "aws_graphite_host" {}
variable "aws_graphite_port" {}
variable "haystack_cluster_name" {}