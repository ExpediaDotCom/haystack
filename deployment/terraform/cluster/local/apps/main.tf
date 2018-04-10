locals {
  app-node_selecter_label = "kubernetes.io/hostname: minikube"
  default_cpu_limit = "100m"
  default_memory_limit = "250Mi"
}

data "terraform_remote_state" "haystack_inrastructure" {
  backend = "local"
  config {
    path = "../state/terraform-infra.tfstate"
  }
}
module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"

  default_cpu_limit = "${local.default_cpu_limit}"
  default_memory_limit = "${local.default_memory_limit}"
  elasticsearch_hostname = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_hostname}"
  elasticsearch_port = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_port}"
  kubectl_context_name = "${data.terraform_remote_state.haystack_inrastructure.k8s_cluster_name}"
  cassandra_hostname = "${data.terraform_remote_state.haystack_inrastructure.cassandra_hostname}"
  cassandra_port = "${data.terraform_remote_state.haystack_inrastructure.cassandra_port}"
  kafka_hostname = "${data.terraform_remote_state.haystack_inrastructure.kafka_hostname}"
  kafka_port = "${data.terraform_remote_state.haystack_inrastructure.kafka_port}"
  graphite_hostname = "${data.terraform_remote_state.haystack_inrastructure.graphite_hostname}"
  graphite_port = "${data.terraform_remote_state.haystack_inrastructure.graphite_port}"
  graphite_enabled = "${data.terraform_remote_state.haystack_inrastructure.graphite_enabled}"
  k8s_app_namespace = "${data.terraform_remote_state.haystack_inrastructure.k8s_app_namespace}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  app-node_selector_label = "${local.app-node_selecter_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"

  #pipes configuration_overrides
  pipes = "${var.pipes}"

  #trace configuration
  traces = "${var.traces}"

  #trends configuration
  trends = "${var.trends}"

  #collector configuration_overrides
  collector = "${var.collector}"

  #ui configuration_overrides
  ui = "${var.ui}"

  #metrictank configuration_overrides
  metrictank = "${var.metrictank}"

}
