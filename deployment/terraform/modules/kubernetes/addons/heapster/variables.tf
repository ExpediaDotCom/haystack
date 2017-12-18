variable "kubectl_executable_name" {}

variable "k8s_heapster_image" {
  default = "gcr.io/google_containers/heapster-amd64:v1.5.0"
}
variable "influxdb_servicename" {
  default = "monitoring-influxdb"
}