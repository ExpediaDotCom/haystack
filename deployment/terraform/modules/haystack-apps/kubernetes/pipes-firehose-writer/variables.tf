variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_hostname" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "enabled" {}
variable "firehose_url" {}
variable "firehose_streamname" {}
variable "firehose_signingregion" {}

variable "node_selecter_label" {
  type = "map"
}
variable "termination_grace_period" {
  default = 30
}
