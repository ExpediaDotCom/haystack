locals {
  graphite_hostname = "monitoring-influxdb-graphite.kube-system.svc"
  graphite_port = 2003
}

module "cassandra" {
  source = "cassandra"
  cassandra_aws_region = "${var.aws_region}"
  cassandra_aws_vpc_id = "${var.aws_vpc_id}"
  cassandra_aws_subnet = "${var.aws_nodes_subnet}"
  cassandra_hosted_zone_id = "${var.aws_hosted_zone_id}"
  cassandra_node_image = "${var.cassandra_node_image}"
  cassandra_node_volume_size = "${var.cassandra_node_volume_size}"
  cassandra_node_count = "${var.cassandra_node_instance_count}"
  cassandra_node_instance_type = "${var.cassandra_node_instance_type}"
  cassandra_ssh_key_pair_name = "${var.aws_ssh_key}"
  cassandra_graphite_host = "${local.graphite_hostname}"
  cassandra_graphite_port = "${local.graphite_port}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

module "es" {
  source = "elasticsearch"
  haystack_index_store_master_instance_count = "${var.haystack_index_store_master_count}"
  haystack_index_store_master_instance_type = "${var.haystack_index_store_es_master_instance_type}"
  haystack_index_store_worker_instance_count = "${var.haystack_index_store_instance_count}"
  haystack_index_store_worker_instance_type = "${var.haystack_index_store_worker_instance_type}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

module "kafka" {
  source = "kafka"
  kafka_aws_vpc_id = "${var.aws_vpc_id}"
  kafka_aws_region = "${var.aws_region}"
  kafka_broker_count = "${var.kafka_broker_count}"
  kafka_broker_instance_type = "${var.kafka_broker_instance_type}"
  kafka_broker_volume_size = "${var.kafka_broker_volume_size}"
  kafka_aws_subnet = "${var.aws_nodes_subnet}"
  kafka_hosted_zone_id = "${var.aws_hosted_zone_id}"
  kafka_ssh_key_pair_name = "${var.aws_ssh_key}"
  kafka_graphite_host = "${local.graphite_hostname}"
  kafka_graphite_port = "${local.graphite_port}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}
