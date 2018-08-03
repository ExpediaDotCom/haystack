locals {
  app-node_selecter_label = "kubernetes.io/hostname: minikube"
}

data "terraform_remote_state" "haystack_inrastructure" {
  backend = "local"
  config {
    path = "../state/terraform-infra.tfstate"
  }
}

module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"

  # FIXME I am getting errors during apply-compose.sh -r uninstall-all:
#  * module.haystack-apps.var.elasticsearch_port: Resource 'data.terraform_remote_state.haystack_inrastructure' does not have attribute 'elasticsearch_port' for variable 'data.terraform_remote_state.haystack_inrastructure.elasticsearch_port'
#  * module.haystack-apps.var.kafka_hostname: Resource 'data.terraform_remote_state.haystack_inrastructure' does not have attribute 'kafka_hostname' for variable 'data.terraform_remote_state.haystack_inrastructure.kafka_hostname'
#  * module.haystack-apps.var.elasticsearch_hostname: Resource 'data.terraform_remote_state.haystack_inrastructure' does not have attribute 'elasticsearch_hostname' for variable 'data.terraform_remote_state.haystack_inrastructure.elasticsearch_hostname'
#  * module.haystack-apps.var.kafka_port: Resource 'data.terraform_remote_state.haystack_inrastructure' does not have attribute 'kafka_port' for variable 'data.terraform_remote_state.haystack_inrastructure.kafka_port'

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

  #service-graph configuration_overrides
  service-graph = "${var.service-graph}"

  #ui configuration_overrides
  ui = "${var.ui}"

  #metrictank configuration_overrides
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
