variable "kubectl_executable_name" {}
variable "kops_executable_name" {}
variable "domain_name" {}

variable "monitoring_addons" {
  type = "map"
  default = {
    enabled = "true"
    graphite_node_port = "32301"
    grafana_storage_volume = "2Gi"
    influxdb_storage_volume = "50Gi"
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
    enabled = "true"
    container_log_path = "/var/lib/docker/containers"
    es_nodes = "1"
    es_storage_volume = "100Gi"
  }
}

variable "cluster" {
  type = "map"
  default = {
    base_domain_name = "local"
    storage_class = "default"
    reverse_proxy_port = "32300"
    monitoring-node_selecter_label = "kops.k8s.io/instancegroup: monitoring-nodes"
    app-node_selecter_label = "kops.k8s.io/instancegroup: app-nodes"
    aws_region = "us-west-2"
    aws_ssh_key = "haystack"
    aws_nodes_subnets = ""
    aws_vpc_id = ""
    aws_utilities_subnet = ""
    aws_s3_bucket_name = ""
    role_prefix = "haystack"
    node_elb_sslcert_arn = ""
    node-elb_ingress = "0.0.0.0/0"
    additional-security_groups = ""
  }
}

variable "common_tags" {
  type = "map"
  default = {
   Product = "Haystack"
  }
}

variable "aa_apps_resource_limits" {
  type = "map"
  default = {
    enabled = true
    cpu_limit = "2"
    memory_limit = "4Gi"
  }
}
