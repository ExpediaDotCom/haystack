variable "kubectl_executable_name" {}
variable "k8s_cluster_name" {}
variable "k8s_logs_es_url" {}
variable "k8s_aws_region" {}

variable "k8s_fluentd_image" {
  default = "cheungpat/fluentd-elasticsearch-aws:1.22"
}

