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
  default = "6d320e806e63a2809b71a5c3e86c535a842789a4"
}

variable "ui_version" {
  default = "581338e50631ce327bcb9db0ab07484d6b0338ec"
}

variable "traces_version" {
  default = "61d53c1cf8d9a8363ab08d73d6815bb6acf1a982"
}


variable "pipes_version" {
  default = "a9faee13c3e0f5069c2af24bb1a6456815580357"
}
