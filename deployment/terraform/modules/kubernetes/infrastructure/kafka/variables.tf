variable "replicas" {}
variable "namespace" {}
variable "enabled" {}
variable "termination_grace_period" {
  default = 30
}
variable "zk_connection_string" {}

