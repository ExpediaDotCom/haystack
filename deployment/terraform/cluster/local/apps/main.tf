locals {
  app-node_selecter_label = "kubernetes.io/hostname: minikube"
}

# FIXME "infrastructure" misspelled below
data "terraform_remote_state" "haystack_inrastructure" {
  backend = "local"
  config {
    path = "../state/terraform-infra.tfstate"
  }
}

module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"

  # FIXME "infrastructure" misspelled below
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


  # ========================================
  # Haystack overrides
  # ========================================

  pipes = "${var.pipes}"
  traces = "${var.traces}"
  trends = "${var.trends}"
  collector = "${var.collector}"
  service-graph = "${var.service-graph}"
  ui = "${var.ui}"
  metrictank = "${var.metrictank}"


  # ========================================
  # Adaptive Alerting overrides
  # ========================================

  alerting = "${var.alerting}"
  ad-mapper = "${var.ad-mapper}"
  ad-manager = "${var.ad-manager}"
  anomaly-validator = "${var.anomaly-validator}"
  aquila-trainer = "${var.aquila-trainer}"

  # Deprecated
  metric-router = "${var.metric-router}"
  constant-detector = "${var.constant-detector}"
  ewma-detector = "${var.ewma-detector}"
  pewma-detector = "${var.pewma-detector}"
}
