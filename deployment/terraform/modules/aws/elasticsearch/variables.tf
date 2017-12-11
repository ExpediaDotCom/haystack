variable "worker_instance_type" {}
variable "master_instance_type" {}
variable "worker_instance_count" {}
variable "master_instance_count" {}

variable "es_version" {
  default = "5.5"
}

variable "domain_name" {
  default = "haystack-index-store"
}

