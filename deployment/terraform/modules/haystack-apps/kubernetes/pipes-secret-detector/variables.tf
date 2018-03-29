variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_hostname" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "enabled" {}
variable "pipes_secret_detector_secretsnotifications_email_from" {}
variable "pipes_secret_detector_secretsnotifications_email_host" {}
variable "pipes_secret_detector_secretsnotifications_email_subject" {}
variable "pipes_secret_detector_secretsnotifications_email_tos" {}

variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_limit"{}
variable "cpu_limit"{}
variable "env_vars" {}
variable "termination_grace_period" {
  default = 30
}
