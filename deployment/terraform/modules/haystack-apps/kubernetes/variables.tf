variable "elasticsearch_hostname" {}
variable "elasticsearch_port" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "cassandra_hostname" {}
variable "cassandra_port" {}
variable "graphite_hostname" {}
variable "k8s_cluster_name" {}
variable "k8s_app_namespace" {}

variable "trends_version" {
  default = "df9b59950fb44a8257db1482cc2ae76a3688d12b"
}

variable "ui_version" {
  default = "459278787c9979855c653c53d66bd181af8aedaa"
}

variable "traces_version" {
  default = "92219da46ca3e3ee20f99eafe2939d8e7dfb004e"
}


variable "pipes_version" {
  default = "5c09d1162a17e7fc815493c6be888122a5372bd0"
}
