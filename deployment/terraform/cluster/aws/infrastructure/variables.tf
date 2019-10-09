variable "kubectl_executable_name" {}
variable "kops_executable_name" {}
variable "domain_name" {}

variable "monitoring_addons" {
  type = "map"
  default = {
    enabled = "true"
    graphite_node_port = "32301"
    grafana_storage_volume = "2Gi"
    grafana_root_url = ""
    influxdb_storage_volume = "50Gi"
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
    enabled = "true"
    container_log_path = "/var/lib/docker/containers"
    es_nodes = "1"
    es_storage_volume = "100Gi"
    logging_backend = "es"  // Backend options for logging: "es" / "splunk". If not specified, default is "es".
    splunk_deployment_server = ""
    splunk_index = ""
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
    node-elb_ingress = "0.0.0.0/0"
    node_ingress = "0.0.0.0/0"
    additional-security_groups = ""
    node_elb_enable_ssl = "true"
  }
}

variable "common_tags" {
  type = "map"
  default = {
   Product = "Haystack"
  }
}
variable "kafka" {
  type = "map"
  default = {
    zookeeper_count = 3
    zookeeper_volume_size = 512
    broker_count = 3
    broker_volume_size = 512
    broker_instance_type = "m4.xlarge"
    default_partition_count = 96
    broker_image = ""
  }
}

variable "kinesis-stream" {
  type = "map"
  default = {
    name = ""
    enabled = false,
    shard_count = 10
    retention_period = 24
    aws_region = "us-west-2"
  }
}

variable "pipes_firehose_stream" {
  type = "map"
  default = {
    name = ""
    enabled = false,
    destination = "s3"
    prefix = ""
    compression_format = "GZIP"
    buffer_size = 128
    buffer_interval = 300
    s3_configuration_bucket_name = ""
  }
}


//Kubernetes cluster created using KOPS
variable "kops_kubernetes" {
  type = "map"
  default = {
    master_instance_type = "c4.large"
    master_instance_volume = 128
    app-nodes_instance_type = "c5.large"
    app-nodes_instance_count = 4
    app-nodes_instance_volume = 256
    monitoring-nodes_instance_type = "m4.xlarge"
    monitoring-nodes_instance_count = 2
    monitoring-nodes_instance_volume = 128
    k8s_version = "1.8.6"
    node_ami = "ami-7ee37206"
    master_ami = "ami-7ee37206"
  }
}

variable "es_spans_index" {
  type = "map"
  default = {
    enabled = false
    dedicated_master_enabled = true
    master_instance_type = "m4.large.elasticsearch"
    master_instance_count = 3
    worker_instance_type = "i3.2xlarge.elasticsearch"
    worker_instance_type = "i3.2xlarge.elasticsearch"
    worker_instnce_count = 3
  }
}

variable "cassandra_spans_backend" {
  type = "map"
  default = {
    node_image = ""
    seed_node_volume_size = 512
    non_seed_node_volume_size = 512
    seed_node_instance_count = 2
    seed_node_instance_count = 2
    non_seed_node_instance_count = 1
    seed_node_instance_type = "c5.xlarge"
    non_seed_node_instance_type = "c5.xlarge"
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

variable "dynamodb" {
  type = "map"
  default = {
    read_limit=10
    write_limit=10
  }
}
