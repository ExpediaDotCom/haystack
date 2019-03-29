locals {
  app-node_selector_label = "kubernetes.io/hostname: minikube"
}

data "terraform_remote_state" "haystack_infrastructure" {
  backend = "local"
  config {
    path = "../state/terraform-infra.tfstate"
  }
}

module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"

  elasticsearch_hostname = "${data.terraform_remote_state.haystack_infrastructure.elasticsearch_hostname}"
  elasticsearch_port = "${data.terraform_remote_state.haystack_infrastructure.elasticsearch_port}"
  kubectl_context_name = "${data.terraform_remote_state.haystack_infrastructure.k8s_cluster_name}"
  cassandra_hostname = "${data.terraform_remote_state.haystack_infrastructure.cassandra_hostname}"
  cassandra_port = "${data.terraform_remote_state.haystack_infrastructure.cassandra_port}"
  kafka_hostname = "${data.terraform_remote_state.haystack_infrastructure.kafka_hostname}"
  kafka_port = "${data.terraform_remote_state.haystack_infrastructure.kafka_port}"
  graphite_hostname = "${data.terraform_remote_state.haystack_infrastructure.graphite_hostname}"
  graphite_port = "${data.terraform_remote_state.haystack_infrastructure.graphite_port}"
  graphite_enabled = "${data.terraform_remote_state.haystack_infrastructure.graphite_enabled}"
  k8s_app_namespace = "${data.terraform_remote_state.haystack_infrastructure.k8s_app_namespace}"
  aa_app_namespace = "${data.terraform_remote_state.haystack_infrastructure.aa_app_namespace}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  app-node_selector_label = "${local.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  domain_name = "${var.domain_name}"

  # Haystack configuration overrides
  pipes = "${var.pipes}"
  traces = "${var.traces}"
  trends = "${var.trends}"
  collector = "${var.collector}"
  service-graph = "${var.service-graph}"
  ui = "${var.ui}"
  metrictank = "${var.metrictank}"
  haystack-alerts = "${var.haystack-alerts}"

  # AA configuration overrides
  alerting = "${var.alerting}"
  modelservice = "${var.modelservice}"
  ad-mapper = "${var.ad-mapper}"
  ad-manager = "${var.ad-manager}"
  mc-a2m-mapper = "${var.mc-a2m-mapper}"
  notifier = "${var.notifier}"

  # Alert Manager
  alert-manager-service = "${var.alert-manager-service}"
  alert-manager-store = "${var.alert-manager-store}"
  alert-manager-notifier = "${var.alert-manager-notifier}"
}
