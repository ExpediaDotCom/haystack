variable "s3_bucket_name" {}
variable "aws_vpc_id" {}
variable "aws_nodes_subnet" {}
variable "aws_utilities_subnet" {}
variable "aws_domain_name" {}
variable "kubectl_executable_name" {}
variable "kops_executable_name" {}
variable "reverse_proxy_port" {
  default = "32300"
}
variable "haystack_cluster_name" {
  default = "haystack"
}
variable "k8s_app_name_space" {
  default = "haystack-apps"
}
variable "traefik_node_port" {
  default = "32300"
}

variable "aws_region" {
  default = "us-west-2"
}

variable "aws_zone" {
  default = "us-west-2c"
}
variable "aws_ssh_key" {
  default = "haystack"
}

variable "haystack_index_store_es_master_instance_type" {
  default = "r3.large.elasticsearch"
}


variable "haystack_index_store_worker_instance_type" {
  default = "r3.large.elasticsearch"
}

variable "haystack_index_store_instance_count" {
  default = 3
}
variable "haystack_index_store_master_count" {
  default = 3
}

variable "kafka_broker_count" {
  default = 4
}
variable "kafka_broker_instance_type" {
  default = "m4.xlarge"
}

variable "k8s_master_instance_type" {
  default = "c4.large"
}

variable "k8s_node_instance_type" {
  default = "m4.large"
}
variable "k8s_node_instance_count" {
  default = 4
}

variable "cassandra_node_image" {
  default = ""
}
variable "cassandra_node_volume_size" {
  default = 32
}
variable "cassandra_node_instance_count" {
  default = 2
}
variable "cassandra_node_instance_type" {
  default = "c4.xlarge"
}


# traces config
variable "traces_enabled" {
  default = true
}
variable "traces_indexer_instances" {
  default = "1"
}
variable "traces_reader_instances" {
  default = "1"
}
variable "traces_version" {
  default = "92219da46ca3e3ee20f99eafe2939d8e7dfb004e"
}

# trends config
variable "trends_enabled" {
  default = true
}
variable "span_timeseries_transformer_instances" {
  default = "1"
}
variable "timeseries_aggregator_instances" {
  default = "1"
}

variable "trends_version" {
  default = "df9b59950fb44a8257db1482cc2ae76a3688d12b"
}

# pipes config
variable "pipes_enabled" {
  default = true
}
variable "pipes_json_transformer_instances" {
  default = "1"
}
variable "pipes_kafka_producer_instances" {
  default = "1"
}
variable "pipes_version" {
  default = "5c09d1162a17e7fc815493c6be888122a5372bd0"
}

# collectors config
variable "kinesis_span_collector_instances" {
  default = "1"
}
variable "kinesis_span_collector_enabled" {
  default = "true"
}
variable "kinesis_span_collector_version" {
  default = "e1d967e30a9a87122d8c332700cc4a3152db7f8a"
}
variable "kinesis_stream_region" {
  default = ""
}
variable "kinesis_stream_name" {
  default = ""
}

# ui config
variable "haystack_ui_instances" {
  default = "1"
}
variable "ui_version" {
  default = "459278787c9979855c653c53d66bd181af8aedaa"
}
