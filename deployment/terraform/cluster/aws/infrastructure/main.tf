locals {
  k8s_datastores_heap_memory_in_mb = "2048"
  graphite_hostname = "monitoring-influxdb-graphite.kube-system.svc"
  graphite_port = 2003
}

module "haystack-k8s" {
  source = "../../../modules/k8s-cluster/aws"
  cluster = "${var.cluster}"
  common_tags = "${var.common_tags}"
  kops_kubernetes = "${var.kops_kubernetes}"
  kops_executable_name = "${var.kops_executable_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  graphite_node_port = "${var.monitoring_addons["graphite_node_port"]}"
}

module "haystack-datastores" {
  source = "../../../modules/haystack-datastores/aws"
  cluster = "${var.cluster}"
  common_tags = "${var.common_tags}"
  kafka = "${var.kafka}"
  kinesis-stream = "${var.kinesis-stream}"
  cassandra_spans_backend = "${var.cassandra_spans_backend}"
  es_spans_index = "${var.es_spans_index}"
  graphite_hostname = "${module.haystack-k8s.external_graphite_hostname}"
  graphite_port = "${local.graphite_port}"
  k8s_nodes_iam-role_arn = "${module.haystack-k8s.nodes_iam-role_arn}"
}
