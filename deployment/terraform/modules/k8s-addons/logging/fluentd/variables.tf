variable "kubectl_executable_name" {}
variable "k8s_cluster_name" {}
variable "elasticsearch_host" {}
variable "elasticsearch_port" {}
variable "container_log_path" {}
variable "k8s_fluentd_image" {
  default = "gcr.io/google-containers/fluentd-elasticsearch:v2.0.2"
}
variable "enabled" {}
