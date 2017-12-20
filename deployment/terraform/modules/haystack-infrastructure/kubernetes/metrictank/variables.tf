variable "namespace" {}
variable "replicas" {}
variable "cassandra_address" {}
variable "kafka_address" {}
variable "graphite_address" {}
variable "termination_grace_period" {
  default = 30
}

