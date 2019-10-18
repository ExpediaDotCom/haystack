variable "kubectl_executable_name" {}

variable "kubectl_context_name" {
  default = "minikube"
}
variable "docker_host_ip" {}

variable "monitoring_addons" {
  type = "map"
  default = {
    enabled = "false"
    graphite_node_port = "32301"
    grafana_storage_volume = "100Mi"
    grafana_root_url = ""
    influxdb_storage_volume = "100Mi"
    influxdb_memory_limit = "2Gi"
    influxdb_cpu_limit = "500"
  }
}

variable "alerting_addons" {
  type = "map"
  default = {
    enabled = "false"
    kubewatch_config_yaml_base64 = ""
  }
}


variable "logging_addons" {
  type = "map"
  default = {
    enabled = "false"
    container_log_path = "/mnt/sda1/var/lib/docker/containers"
    es_nodes = "1"
    es_storage_volume = "100Mi"
    logging_backend = ""  // Backend options for logging: "es" / "splunk". If not specified, default is "es".
    splunk_deployment_server = ""
    splunk_index = ""
  }
}


variable "cluster" {
  type = "map"
  default = {
    name = "haystack"
    domain_name = "local"
    storage_class = "default"
    reverse_proxy_port = "32300"
    monitoring-node_selecter_label = "kubernetes.io/hostname: minikube"
    app-node_selecter_label = "kubernetes.io/hostname: minikube"
    role_prefix = "haystack"
  }
}

variable "aa_apps_resource_limits" {
  type = "map"
  default = {
    enabled = false
    cpu_limit = "1"
    memory_limit = "1Gi"
  }
}