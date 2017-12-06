variable "region" {}

variable "es_version" {
  default = "5.5"
}

variable "domain_name" {
  default = "haystack-index-store"
}

variable "worker_instance_type" {
  default = "r3.large.elasticsearch"
}

variable "master_instance_type" {
  default = "r3.large.elasticsearch"
}

variable "worker_instance_count" {}
variable "master_instance_count" {}
