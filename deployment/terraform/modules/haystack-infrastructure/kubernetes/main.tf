locals {
  graphite_hostname = "monitoring-influxdb-graphite.kube-system.svc"
  graphite_port = 2003
}

module "cassandra" {
  source = "cassandra"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
}

module "es" {
  source = "elasticsearch"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
}

module "zookeeper" {
  source = "zookeeper"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
}

module "kafka" {
  source = "kafka"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
  zk_connection_string = "${module.zookeeper.zookeeper_service_name}:${module.zookeeper.zookeeper_service_port}"
}

module "metrictank" {
  source = "metrictank"
  replicas = "1"
  cassandra_address = "${module.cassandra.cassandra_hostname}:${module.cassandra.cassandra_port}"
  kafka_address = "${module.kafka.kafka_service_name}:${module.kafka.kafka_port}"
  namespace = "${var.k8s_app_name_space}"
  graphite_address = "${local.graphite_hostname}:${local.graphite_port}"
}