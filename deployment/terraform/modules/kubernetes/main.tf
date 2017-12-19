resource "kubernetes_namespace" "haystack-app-namespace" {
  metadata {
    name = "${var.k8s_app_name_space}"
  }
}

module "kubernetes-addons" {
  source = "addons"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_logs_es_url = "${var.k8s_logs_es_url}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  haystack_app_namespace = "${kubernetes_namespace.haystack-app-namespace.metadata.0.name}"
  haystack_domain_name = "${var.haystack_domain_name}"
  traefik_node_port = "${var.traefik_node_port}"
}


module "haystack-infrastructure" {
  source = "infrastructure"
  k8s_app_name_space = "${kubernetes_namespace.haystack-app-namespace.metadata.0.name}"
  enabled = "${var.enable_docker_infrastructure}"
}

module "kubernetes-apps" {
  source = "apps"
  k8s_app_name_space = "${kubernetes_namespace.haystack-app-namespace.metadata.0.name}"
  zookeeper_service_port = "${module.haystack-infrastructure.zookeeper_service_port}"
  elasticsearch_service_port = "${module.haystack-infrastructure.elasticsearch_service_port}"
  kafka_port = "${module.haystack-infrastructure.kafka_port}"
  cassandra_port = "${module.haystack-infrastructure.cassandra_port}"
  elasticsearch_service_name = "${module.haystack-infrastructure.elasticsearch_service_name}"
  zookeeper_service_name = "${module.haystack-infrastructure.zookeeper_service_name}"
  cassandra_service_name = "${module.haystack-infrastructure.cassandra_service_name}"
  kafka_hostname = "${module.haystack-infrastructure.kafka_service_name}"
  graphite_hostname = "${module.haystack-infrastructure.graphite_hostname}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
}