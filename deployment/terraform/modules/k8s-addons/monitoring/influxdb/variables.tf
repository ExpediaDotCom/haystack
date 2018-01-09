variable "kubectl_executable_name" {}
variable "enabled" {}
variable "k8s_cluster_name" {}

variable "storage_volume" {}

variable "storage_class" {}

variable "k8s_influxdb_image" {
  default = "gcr.io/google_containers/heapster-influxdb-amd64:v1.3.3"
}

