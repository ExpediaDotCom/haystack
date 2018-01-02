variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_hostname" {}
variable "graphite_hostname" {}
variable "enabled"{}

variable "termination_grace_period" {
  default = 30
}
