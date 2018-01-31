variable "namespace" {}
variable "replicas" {}
variable "cassandra_address" {}
variable "kafka_address" {}
variable "graphite_address" {}
variable "node_selecter_label" {
  type = "map"
}

variable "termination_grace_period" {
  default = 30
}
variable "enabled" {}


