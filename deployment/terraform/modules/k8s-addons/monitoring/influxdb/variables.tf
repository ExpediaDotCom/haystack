variable "kubectl_executable_name" {}
variable "enabled" {}
variable "k8s_cluster_name" {}

variable "k8s_influxdb_storage" {
  default = "100Mi"
}

variable "k8s_influxdb_storage_class" {
  default = "standard"
}

variable "k8s_influxdb_image" {
  default = "gcr.io/google_containers/heapster-influxdb-amd64:v1.3.3"
}

