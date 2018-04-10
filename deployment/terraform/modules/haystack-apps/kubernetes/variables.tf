variable "elasticsearch_hostname" {}
variable "elasticsearch_port" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "cassandra_hostname" {}
variable "cassandra_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "haystack_cluster_name" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "k8s_app_namespace" {}
variable "app-node_selector_label"{}

variable "default_memory_limit"{}
variable "default_cpu_limit"{}

# traces config
variable "traces" {
  type = "map"
}

# trends config
variable "trends" {
  type = "map"
}


# pipes config
variable "pipes" {
  type = "map"
}


# collectors config
variable "collector" {
  type = "map"
}

# ui config
variable "ui" {
  type = "map"
}

#metrictank
variable "metrictank" {
  type = "map"
}
