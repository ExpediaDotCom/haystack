variable "kubectl_executable_name" {}

variable "k8s_influxdb_storage" {
  default = "100Mi"
}

variable "k8s_influxdb_storage_class" {
  default = "standard"
}

variable "k8s_influxdb_image" {
  default = "gcr.io/google_containers/heapster-amd64:v1.4.2"
}

