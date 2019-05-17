locals {
  app-node_selecter_label = "kops.k8s.io/instancegroup: app-nodes"
  //always setting to true for aws deployment
  graphite_enabled = "true"
}

data "terraform_remote_state" "haystack_infrastructure" {
  backend = "s3"
  config {
    bucket = "${var.s3_bucket_name}"
    key = "terraform/${var.haystack_cluster_name}-infrastructure"
    region = "us-west-2"
  }
}

module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"
  k8s_app_namespace = "${data.terraform_remote_state.haystack_infrastructure.k8s_app_namespace}"
  aa_app_namespace = "${data.terraform_remote_state.haystack_infrastructure.aa_app_namespace}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  app-node_selector_label = "${local.app-node_selecter_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"

  elasticsearch_hostname = "${data.terraform_remote_state.haystack_infrastructure.elasticsearch_hostname}"
  elasticsearch_port = "${data.terraform_remote_state.haystack_infrastructure.elasticsearch_port}"
  kubectl_context_name = "${data.terraform_remote_state.haystack_infrastructure.k8s_cluster_name}"
  cassandra_hostname = "${data.terraform_remote_state.haystack_infrastructure.cassandra_hostname}"
  kafka_hostname = "${data.terraform_remote_state.haystack_infrastructure.kafka_hostname}"
  kafka_port = "${data.terraform_remote_state.haystack_infrastructure.kafka_port}"
  cassandra_port = "${data.terraform_remote_state.haystack_infrastructure.cassandra_port}"
  graphite_hostname = "${data.terraform_remote_state.haystack_infrastructure.graphite_hostname}"
  graphite_port = "${data.terraform_remote_state.haystack_infrastructure.graphite_port}"
  graphite_enabled = "${local.graphite_enabled}"
  domain_name = "${var.domain_name}"
  kinesis-stream_name = "${data.terraform_remote_state.haystack_infrastructure.kinesis-stream_name}"
  kinesis-stream_region = "${data.terraform_remote_state.haystack_infrastructure.kinesis-stream_region}"

  # Haystack configuration overrides
  pipes = "${var.pipes}"
  traces = "${var.traces}"
  trends = "${var.trends}"
  collector = "${var.collector}"
  service-graph = "${var.service-graph}"
  ui = "${var.ui}"
  metrictank = "${var.metrictank}"
  haystack-alerts = "${var.haystack-alerts}"

  # Adaptive Alerting configuration overrides
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

  # Pitchfork
  pitchfork = "${var.pitchfork}"
}
