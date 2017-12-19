variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}

variable "termination_grace_period" {
  default = 30
}
