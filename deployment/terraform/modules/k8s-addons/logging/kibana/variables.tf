variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "elasticsearch_http_endpoint" {}
variable "monitoring-node_selecter_label" {}
variable "enabled" {}
variable "logs_cname" {}

variable "k8s_fluentd_image" {
  default = "cheungpat/fluentd-elasticsearch-aws:1.22"
}