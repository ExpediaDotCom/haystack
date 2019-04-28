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
    influxdb_storage_volume = "100Mi"
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
    vpce-svc_enabled = false
    vpce-acceptance_required = false
    vpce-proxy_port = "32300"
    vpce-allowed_principals = ""
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