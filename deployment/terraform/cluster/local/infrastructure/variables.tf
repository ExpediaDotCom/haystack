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
    haystack_grafana_dashboard_image = "expediadotcom/haystack-grafana-dashboards"
    k8s_influxdb_image = "influxdb:1.6"
  }
}

variable "alerting_addons" {
  type = "map"
  default = {
    enabled = "false"
    kubewatch_config_yaml_base64 = ""
    kubewatch_image = "tuna/kubewatch:v0.0.1"
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
    curator_image = "bobrik/curator:5.4.0"
    kibana_logging_image = "docker.elastic.co/kibana/kibana:5.6.5"
    splunk_forwarder_image = "splunk/universalforwarder:6.6.3"
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
    k8s_traefik_image = "traefik:v1.7.3"
  }
}

variable "traefik_addon" {
  type = "map"
  default = {
    k8s_traefik_image = "traefik:v1.7.3"
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