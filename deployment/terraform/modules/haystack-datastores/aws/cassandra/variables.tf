variable "cassandra_node_count" {}
variable "cassandra_node_volume_size" {}
variable "cassandra_node_instance_type" {}
variable "cassandra_node_image" {
  default = ""
}
variable "cassandra_aws_vpc_id" {}
variable "cassandra_aws_region" {}
variable "cassandra_aws_subnet" {}
variable "cassandra_hosted_zone_id" {}
variable "cassandra_ssh_key_pair_name" {}
variable "cassandra_graphite_host" {}
variable "cassandra_graphite_port" {}