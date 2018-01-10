//when running locally we expect the machine to have a local k8s cluster using minikube
locals {
  container_log_path = "/mnt/sda1/var/lib/docker/containers"
}
module "k8s-addons" {
  source = "../../modules/k8s-addons"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  traefik_node_port = "${var.reverse_proxy_port}"
  k8s_app_namespace = "${var.k8s_app_name_space}"
  haystack_domain_name = "${var.haystack_domain_name}"
  add_logging_addons = false
  add_monitoring_addons = false
  container_log_path = "${local.container_log_path}"
  logging_es_nodes = "1"
}

module "haystack-infrastructure" {
  source = "../../modules/haystack-infrastructure/kubernetes"
  k8s_app_name_space = "${module.k8s-addons.k8s_app_namespace}"
}
module "haystack-apps" {
  source = "../../modules/haystack-apps/kubernetes"
  kafka_port = "${module.haystack-infrastructure.kafka_port}"
  elasticsearch_hostname = "${module.haystack-infrastructure.elasticsearch_hostname}"
  elasticsearch_port = "${module.haystack-infrastructure.elasticsearch_port}"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  cassandra_hostname = "${module.haystack-infrastructure.cassandra_hostname}"
  kafka_hostname = "${module.haystack-infrastructure.kafka_hostname}"
  cassandra_port = "${module.haystack-infrastructure.kafka_port}"
  metrictank_hostname = "${module.haystack-infrastructure.metrictank_hostname}"
  metrictank_port = "${module.haystack-infrastructure.metrictank_port}"
  graphite_hostname = "${module.haystack-infrastructure.graphite_hostname}"
  k8s_app_namespace = "${module.k8s-addons.k8s_app_namespace}"

  pipes_enabled = "${var.pipes_enabled}"
  pipes_json_transformer_instances = "${var.pipes_json_transformer_instances}"
  pipes_kafka_producer_instances = "${var.pipes_kafka_producer_instances}"
  pipes_version = "${var.pipes_version}"

  traces_enabled = "${var.traces_enabled}"
  traces_version = "${var.traces_version}"
  traces_indexer_instances = "${var.traces_indexer_instances}"
  traces_reader_instances = "${var.traces_reader_instances}"

  trends_enabled = "${var.trends_enabled}"
  trends_version = "${var.trends_version}"
  span_timeseries_transformer_instances = "${var.span_timeseries_transformer_instances}"
  timeseries_aggregator_instances = "${var.timeseries_aggregator_instances}"

  ui_version = "${var.ui_version}"
  haystack_ui_instances = "${var.haystack_ui_instances}"

}
