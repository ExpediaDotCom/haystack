variable "s3_bucket_name" {}
variable "aws_vpc_id" {}
variable "aws_nodes_subnet" {}
variable "aws_nodes_subnets" {
  type = "list"
}
variable "aws_utilities_subnet" {}
variable "aws_domain_name" {}
variable "kubectl_executable_name" {}
variable "kops_executable_name" {}
variable "haystack_cluster_name" {}


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
    name = "haystack"
    base_domain_name = "local"
    storage_class = "default"
    reverse_proxy_port = "32300"
    monitoring-node_selecter_label = "kops.k8s.io/instancegroup: monitoring-nodes"
    app-node_selecter_label = "kops.k8s.io/instancegroup: app-nodes"
  }
}

variable "aws_region" {
  default = "us-west-2"
}

variable "aws_ssh_key" {
  default = "haystack"
}

variable "es_master_instance_type" {
  default = "m4.large.elasticsearch"
}


variable "es_worker_instance_type" {
  default = "i3.2xlarge.elasticsearch"
}

variable "es_worker_instance_count" {
  default = 3
}

variable "es_master_instance_count" {
  default = 3
}

variable "es_dedicated_master_enabled" {
  default = true
}

variable "zookeeper_count" {
  default = 3
}
variable "zookeeper_volume_size" {
  default = 512
}
variable "kafka_broker_count" {
  default = 1
}
variable "kafka_broker_volume_size" {
  default = 512
}
variable "kafka_broker_instance_type" {
  default = "m4.xlarge"
}

variable "k8s_master_instance_type" {
  default = "c4.large"
}
variable "kafka_default_partition_count" {
  default = 96
}


variable "k8s_app-nodes_instance_type" {
  default = "c5.large"
}
variable "k8s_app-nodes_instance_count" {
  default = 4
}
variable "k8s_monitoring-nodes_instance_type" {
  default = "m4.xlarge"
}
variable "k8s_monitoring-nodes_instance_count" {
  default = 2
}
variable "cassandra_node_image" {
  default = ""
}
variable "cassandra_seed_node_volume_size" {
  default = 512
}
variable "cassandra_non_seed_node_volume_size" {
  default = 512
}
variable "cassandra_seed_node_instance_count" {
  default = 2
}
variable "cassandra_non_seed_node_instance_count" {
  default = 1
}
variable "cassandra_seed_node_instance_type" {
  default = "c4.xlarge"
}
variable "cassandra_non_seed_node_instance_type" {
  default = "c4.xlarge"
}

variable "aa_apps_resource_limits" {
  type = "map"
  default = {
    enabled = true
    cpu_limit = "2"
    memory_limit =  "4Gi"
  }
}
