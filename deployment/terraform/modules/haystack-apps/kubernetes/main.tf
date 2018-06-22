module "traces" {
  source = "traces"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  kafka_port = "${var.kafka_port}"
  elasticsearch_port = "${var.elasticsearch_port}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  cassandra_hostname = "${var.cassandra_hostname}"
  cassandra_port = "${var.cassandra_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  traces = "${var.traces}"
}

module "trends" {
  source = "trends"
  app_namespace = "${var.k8s_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"

  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  cassandra_hostname = "${var.cassandra_hostname}"
  cassandra_port = "${var.cassandra_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  trends = "${var.trends}"
  metrictank = "${var.metrictank}"
}

module "pipes" {
  source = "pipes"
  app_namespace = "${var.k8s_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"
  haystack_cluster_name = "${var.haystack_cluster_name}"

  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"

  pipes = "${var.pipes}"
}

module "collectors" {
  source = "collectors"

  app_namespace = "${var.k8s_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"
  haystack_cluster_name = "${var.haystack_cluster_name}"

  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"

  collector = "${var.collector}"
}

module "service-graph" {
  source = "service-graph"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  kafka_port = "${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  service-graph = "${var.service-graph}"
}

module "ui" {
  source = "ui"
  enabled = "${var.ui["enabled"]}"
  image = "expediadotcom/haystack-ui:${var.ui["version"]}"
  replicas = "${var.ui["instances"]}"
  namespace = "${var.k8s_app_namespace}"
  k8s_cluster_name = "${var.kubectl_context_name}"
  node_selecter_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  trace_reader_hostname = "${module.traces.reader_hostname}"
  trace_reader_service_port = "${module.traces.reader_port}"
  metrictank_hostname = "${module.trends.metrictank_hostname}"
  metrictank_port = "${module.trends.metrictank_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  cpu_limit = "${var.ui["cpu_limit"]}"
  cpu_request = "${var.ui["cpu_request"]}"
  memory_limit = "${var.ui["memory_limit"]}"
  memory_request = "${var.ui["memory_request"]}"
  whitelisted_fields = "${var.ui["whitelisted_fields"]}"
  ui_enable_sso = "${var.ui["enable_sso"]}"
  ui_saml_issuer = "${var.ui["saml_issuer"]}"
  ui_saml_callback_url = "${var.ui["saml_callback_url"]}"
  ui_session_secret = "${var.ui["session_secret"]}"
  ui_saml_entry_point = "${var.ui["saml_entry_point"]}"
}

module "alerting" {
  source = "alerting"

  app_namespace = "${var.k8s_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"
  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  metrictank = "${var.metrictank}"
  alerting = "${var.alerting}"
  metric-router = "${var.metric-router}"
  ewma-detector = "${var.ewma-detector}"
  constant-detector = "${var.constant-detector}"
  pewma-detector = "${var.pewma-detector}"
  anomaly-validator = "${var.anomaly-validator}"
}