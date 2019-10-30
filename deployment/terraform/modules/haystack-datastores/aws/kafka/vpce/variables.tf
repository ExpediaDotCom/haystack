variable "subnets" {
  type = "list"
}

variable "cluster" {
  type = "map"
}

variable "kafka" {
  type = "map"
}

variable "kafka_port" {}

variable "vpce_whitelisted_accounts" {
  type = "list"
}

variable "common_tags" {
  type = "map"
}

variable "kafka_instance_ids" {
  type = "list"
}
