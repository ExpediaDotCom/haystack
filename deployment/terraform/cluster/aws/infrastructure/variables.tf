variable "s3_bucket_name" {}
variable "aws_vpc_id" {}
variable "aws_nodes_subnet" {}
variable "aws_utilities_subnet" {}
variable "aws_domain_name" {}
variable "kubectl_executable_name" {}
variable "kops_executable_name" {}
variable "haystack_cluster_name" {}

variable "kubewatch_enabled" {
  default = false
}

variable "kubewatch_config_yaml_base64" {
  default = ""
}

variable "traefik_node_port" {
  default = "32300"
}

variable "graphite_node_port" {
  default = "32301"
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
/*variable "cassandra_node_volume_size" {
  default = 512
}*/
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
/*variable "cassandra_node_instance_type" {
  default = "c4.xlarge"
}*/
variable "cassandra_seed_node_instance_type" {
  default = "c4.xlarge"
}
variable "cassandra_non_seed_node_instance_type" {
  default = "c4.xlarge"
}
