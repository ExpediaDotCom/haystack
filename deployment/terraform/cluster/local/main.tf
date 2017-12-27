//when running locally we expect the machine to have a local k8s cluster using minikube

module "k8s-addons" {
  source = "../../modules/k8s-addons"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  traefik_node_port = "${var.reverse_proxy_port}"
  k8s_app_namespace = "${var.k8s_app_name_space}"
  haystack_domain_name = "${var.haystack_domain_name}"
  add_logging_addons = true
  add_monitoring_addons = false
}

module "haystack-infrastructure" {
  source = "../../modules/haystack-infrastructure/kubernetes"
  k8s_app_name_space = "${module.k8s-addons.k8s_app_namespace}"
}
module "haystack-apps" {
  source = "../../modules/haystack-apps/kubernetes"
  kafka_port = "${module.haystack-infrastructure.kafka_port}"
  elasticsearch_port = "${module.haystack-infrastructure.elasticsearch_port}"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  cassandra_hostname = "${module.haystack-infrastructure.cassandra_hostname}"
  kafka_hostname = "${module.haystack-infrastructure.kafka_hostname}"
  cassandra_port = "${module.haystack-infrastructure.kafka_port}"
  elasticsearch_hostname = "${module.haystack-infrastructure.kafka_port}"
  graphite_hostname = "${module.haystack-infrastructure.graphite_hostname}"
  k8s_app_namespace = "${module.k8s-addons.k8s_app_namespace}"
}
