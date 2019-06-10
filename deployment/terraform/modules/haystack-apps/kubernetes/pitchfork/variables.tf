variable "pitchfork" {
  type = "map"
}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "enable_kafka_sink" {
  default = false
}
variable "termination_grace_period" {
  default = 30
}
variable "namespace" {}
variable "kafka_hostname" {} 
variable "kafka_port" {} 
variable "env_vars" {}
variable "domain_name" {}