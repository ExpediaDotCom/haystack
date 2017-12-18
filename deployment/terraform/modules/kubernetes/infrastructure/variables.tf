variable "k8s_app_name_space" {
  default = "haystack-apps"
}
variable "es_docker_image" {
  default = "elasticsearch:2.4.6-alpine"
}
variable "cassandra_docker_image" {
  default = "cassandra:3.11.0"
}
variable "kafka_docker_image" {
  default = "wurstmeister/kafka:0.10.2.1"
}
variable "zookeeper_docker_image" {
  default = "wurstmeister/zookeeper:3.4.6"
}
variable "metrictank_docker_image" {
  default = "raintank/metrictank:latest"
}

variable "enabled"{}