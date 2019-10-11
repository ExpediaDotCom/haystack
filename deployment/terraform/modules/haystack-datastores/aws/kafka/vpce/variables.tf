variable "subnets" {
  type = "list"
}

variable "cluster" {
  type = "map"
}

variable "kafka" {
  type = "map"
}

variable "common_tags" {
  type = "map"
}

variable "kafka_instance_ids" {
  type = "list"
}

variable "whitelisted_accounts" {
  type = "list"
  default = []
}
