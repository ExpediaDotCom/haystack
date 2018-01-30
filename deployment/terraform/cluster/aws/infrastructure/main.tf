locals {
  container_log_path = "/var/lib/docker/containers"
  haystack_ui_cname = "${var.haystack_cluster_name}.${var.aws_domain_name}"
  metrics_cname = "${var.haystack_cluster_name}-metrics.${var.aws_domain_name}"
  logs_cname = "${var.haystack_cluster_name}-logs.${var.aws_domain_name}"
  k8s_dashboard_cname = "${var.haystack_cluster_name}-k8s.${var.aws_domain_name}"
}

module "haystack-k8s" {
  source = "../../../modules/k8s-cluster/aws"
  aws_ssh_key = "${var.aws_ssh_key}"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"
  aws_domain_name = "${var.aws_domain_name}"
  master_instance_type = "${var.k8s_master_instance_type}"
  aws_vpc_id = "${var.aws_vpc_id}"
  aws_nodes_subnet = "${var.aws_nodes_subnet}"
  aws_utility_subnet = "${var.aws_utilities_subnet}"
  s3_bucket_name = "${var.s3_bucket_name}"
  app-node_instance_count = "${var.k8s_app-nodes_instance_count}"
  app-node_instance_type = "${var.k8s_app-nodes_instance_type}"
  "monitoring-nodes_instance_count" = "${var.k8s_monitoring-nodes_instance_count}"
  "monitoring-nodes_instance_type" = "${var.k8s_monitoring-nodes_instance_type}"
  reverse_proxy_port = "${var.traefik_node_port}"
  kops_executable_name = "${var.kops_executable_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  metrics_cname_enabled = true
  metrics_cname = "${local.metrics_cname}"
  logs_cname_enabled = true
  logs_cname = "${local.logs_cname}"
  k8s_dashboard_cname_enabled = true
  k8s_dashboard_cname = "${local.k8s_dashboard_cname}"
  haystack_ui_cname = "${local.haystack_ui_cname}"
  graphite_node_port = "${var.graphite_node_port}"
}

module "k8s-addons" {
  source = "../../../modules/k8s-addons"
  kubectl_context_name = "${module.haystack-k8s.cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  traefik_node_port = "${var.traefik_node_port}"
  graphite_node_port = "${var.graphite_node_port}"
  base_domain_name = "${var.aws_domain_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  add_monitoring_addons = true
  add_logging_addons = true
  add_k8s_dashboard_addons = true
  container_log_path = "${local.container_log_path}"
  logging_es_nodes = "1"
  k8s_storage_class = "default"
  grafana_storage_volume = "2Gi"
  influxdb_storage_volume = "2Gi"
  es_storage_volume = "100Gi"
  logs_cname = "${local.logs_cname}"
  k8s_dashboard_cname = "${local.k8s_dashboard_cname}"
  haystack_ui_cname = "${local.haystack_ui_cname}"
  metrics_cname = "${local.metrics_cname}"
}

module "haystack-datastores" {
  source = "../../../modules/haystack-datastores/aws"
  aws_utilities_subnet = "${var.aws_utilities_subnet}"
  aws_vpc_id = "${var.aws_vpc_id}"
  aws_nodes_subnet = "${var.aws_nodes_subnet}"
  s3_bucket_name = "${var.s3_bucket_name}"
  aws_ssh_key = "${var.aws_ssh_key}"
  zookeeper_count = "${var.zookeeper_count}"
  zookeeper_volume_size = "${var.zookeeper_volume_size}"
  kafka_broker_instance_type = "${var.kafka_broker_instance_type}"
  kafka_broker_count = "${var.kafka_broker_count}"
  kafka_broker_volume_size = "${var.kafka_broker_volume_size}"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"
  cassandra_node_volume_size = "${var.cassandra_node_volume_size}"
  cassandra_seed_node_instance_count = "${var.cassandra_seed_node_instance_count}"
  cassandra_non_seed_node_instance_count = "${var.cassandra_non_seed_node_instance_count}"
  cassandra_node_instance_type = "${var.cassandra_node_instance_type}"
  cassandra_node_image = "${var.cassandra_node_image}"
  k8s_app_name_space = "${module.k8s-addons.k8s_app_namespace}"
  haystack_index_store_master_count = "${var.haystack_index_store_master_count}"
  haystack_index_store_instance_count = "${var.haystack_index_store_instance_count}"
  haystack_index_store_worker_instance_type = "${var.haystack_index_store_worker_instance_type}"
  haystack_index_store_es_master_instance_type = "${var.haystack_index_store_es_master_instance_type}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  graphite_hostname = "${module.haystack-k8s.external_graphite_hostname}"
  graphite_port = "${module.k8s-addons.graphite_port}"
}