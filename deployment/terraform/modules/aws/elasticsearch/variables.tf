variable "haystack_index_store_worker_instance_type" {}
variable "haystack_index_store_master_instance_type" {}
variable "haystack_index_store_worker_instance_count" {}
variable "haystack_index_store_master_instance_count" {}
variable "haystack_logs_instance_type" {}
variable "haystack_logs_instance_count" {}

variable "haystack_index_store_es_version" {
  default = "5.5"
}

variable "haystack_index_store_domain_name" {
  default = "haystack-index-store"
}


variable "haystack_logs_es_version" {
  default = "5.5"
}

variable "haystack_logs_domain_name" {
  default = "haystack-logs"
}


