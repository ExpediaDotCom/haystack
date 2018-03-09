variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_hostname" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "enabled" {}
variable "firehose_url" {}
variable "firehose_streamname" {}
variable "firehose_kafka_threadcount" {}
variable "firehose_signingregion" {}
variable "firehose_retrycount" {}

variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_limit"{}
variable "cpu_limit"{}
variable "env_vars" {}
variable "termination_grace_period" {
  default = 30
}
