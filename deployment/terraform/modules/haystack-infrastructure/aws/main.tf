locals {
  graphite_hostname = "monitoring-influxdb-graphite.kube-system.svc"
  graphite_port = 2003
}

module "cassandra" {
  source = "cassandra"
  cassandra_security_group = ""
  cassandra_hosted_zone_id = ""
  cassandra_aws_vpc_id = ""
  cassandra_aws_subnet = ""
  cassandra_node_count = ""
  cassandra_ssh_key_pair_name = ""
  cassandra_node_volume_size = ""
  cassandra_node_instance_type = ""
  cassandra_node_image = ""
  cassandra_aws_region = ""
}

module "es" {
  source = "elasticsearch"
  haystack_index_store_master_instance_count = "${var.haystack_index_store_master_count}"
  haystack_index_store_master_instance_type = "${var.haystack_index_store_es_master_instance_type}"
  haystack_index_store_worker_instance_count = "${var.haystack_index_store_instance_count}"
  haystack_index_store_worker_instance_type = "${var.haystack_index_store_worker_instance_type}"
}

module "kafka" {
  source = "kafka"
  kafka_aws_ssh_key = "${var.aws_ssh_key}"
  kafka_aws_region = "${var.aws_region}"
  kafka_broker_count = "${var.kafka_broker_count}"
  kafka_aws_vpc_id = "${var.aws_vpc_id}"
  kafka_broker_instance_type = "${var.kafka_broker_instance_type}"
  kafka_aws_subnet = "${var.aws_nodes_subnet}"
}

//metrictank is still deployed as a container might consider inside the k8s cluster
module "metrictank" {
  source = "../kubernetes/metrictank"
  replicas = "1"
  cassandra_address = "${module.cassandra.cassandra_hostname}:${module.cassandra.cassandra_port}"
  kafka_address = "${module.kafka.kafka_service_name}:${module.kafka.kafka_port}"
  namespace = "${var.k8s_app_name_space}"
  graphite_address = "${local.graphite_hostname}:${local.graphite_port}"
}