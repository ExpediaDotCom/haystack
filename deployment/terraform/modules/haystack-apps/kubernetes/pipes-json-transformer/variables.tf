variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_hostname" {}
variable "enabled"{}
variable "graphite_hostname" {}
variable "graphite_port" {}

variable "node_selecter_label" {
  type = "map"
}
variable "termination_grace_period" {
  default = 30
}
