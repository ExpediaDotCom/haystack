variable "image" {}
variable "replicas" {}
variable "enabled"{}
variable "namespace" {}
variable "kinesis_stream_region" {}
variable "kinesis_stream_name" {}
variable "sts_role_arn" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "kafka_endpoint" {}
variable "node_selecter_label" {
  type = "map"
}
variable "termination_grace_period" {
  default = 30
}
variable "haystack_cluster_name" {}