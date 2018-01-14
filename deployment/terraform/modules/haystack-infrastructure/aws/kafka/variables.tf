variable "kafka_broker_count" {}
variable "kafka_broker_volume_size" {}
variable "kafka_broker_instance_type" {}
variable "kafka_broker_image" {
  default = ""
}
variable "kafka_aws_vpc_id" {}
variable "kafka_aws_region" {}
variable "kafka_aws_subnet" {}
variable "kafka_hosted_zone_id" {}
variable "kafka_ssh_key_pair_name" {}
variable "kafka_graphite_host" {}
variable "kafka_graphite_port" {}
