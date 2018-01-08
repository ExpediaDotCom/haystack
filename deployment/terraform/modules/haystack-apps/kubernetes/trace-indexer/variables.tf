variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}
variable "elasticsearch_endpoint" {}
variable "cassandra_hostname" {}
variable "enabled"{}

variable "termination_grace_period" {
  default = 30
}
