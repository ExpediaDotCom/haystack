locals {
  k8s_datastores_heap_memory_in_mb = "2048"
}

module "haystack-k8s" {
  source = "../../../modules/k8s-cluster/aws"
  cluster = "${var.cluster}"
  kops_kubernetes = "${var.kops_kubernetes}"
  kops_executable_name = "${var.kops_executable_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  graphite_node_port = "${var.monitoring_addons["graphite_node_port"]}"
}

module "k8s-addons" {
  source = "../../../modules/k8s-addons"
  kubectl_context_name = "${module.haystack-k8s.cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  datastores_heap_memory_in_mb = "${local.k8s_datastores_heap_memory_in_mb}"
  aa_apps_resource_limits = "${var.aa_apps_resource_limits}"
  monitoring_addons = "${var.monitoring_addons}"
  alerting_addons = "${var.alerting_addons}"
  logging_addons = "${var.logging_addons}"
  cluster = "${var.cluster}"
}

module "haystack-datastores" {
  source = "../../../modules/haystack-datastores/aws"
  cluster = "${var.cluster}"
  kafka = "${var.kafka}"
  kinesis-stream = "${var.kinesis-stream}"
  cassandra_spans_backend = "${var.cassandra_spans_backend}"
  es_spans_index = "${var.es_spans_index}"
  graphite_hostname = "${module.haystack-k8s.external_graphite_hostname}"
  graphite_port = "${module.k8s-addons.graphite_port}"
  k8s_nodes_iam-role_arn = "${module.haystack-k8s.nodes_iam-role_arn}"
}
