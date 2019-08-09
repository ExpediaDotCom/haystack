variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "namespace" {}
variable "node_selector_label"{}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}

variable "haystack-agent" {
  type = "map"
}

variable "aws_region" {
  default = "us-west-2"
}

variable "spans_service_port" {
  default = 34000
}

variable "blobs_service_port" {
  default = 34001
}