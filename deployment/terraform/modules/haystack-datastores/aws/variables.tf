
variable "cluster" {
  type = "map"
}

variable "kafka" {
  type = "map"
}

variable "es_spans_index" {
  type = "map"
}

variable "kinesis-stream" {
  type = "map"
}

variable "cassandra_spans_backend" {
  type = "map"
}

variable "graphite_hostname" {}
variable "graphite_port" {}
variable "k8s_nodes_iam-role_arn" {}

