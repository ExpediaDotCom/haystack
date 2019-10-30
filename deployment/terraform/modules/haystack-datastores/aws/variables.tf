
variable "cluster" {
  type = "map"
}

variable "kafka" {
  type = "map"
}

variable "kafka_vpce_whitelisted_accounts" {
  type = "list"
}

variable "es_spans_index" {
  type = "map"
}

variable "kinesis-stream" {
  type = "map"
}
variable "pipes_firehose_stream" {
  type = "map"
}

variable "dynamodb" {
  type = "map"
}

variable "common_tags" {
  type = "map"
}
variable "cassandra_spans_backend" {
  type = "map"
}

variable "graphite_hostname" {}
variable "graphite_port" {}
variable "k8s_nodes_iam-role_arn" {}

