module "traces" {
  source = "github.com/ExpediaDotCom/haystack-traces/deployment/terraform"
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
  source = "github.com/ExpediaDotCom/haystack-trends/deployment/terraform"
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
  source = "github.com/ExpediaDotCom/haystack-pipes/deployment/terraform"
  app_namespace = "${var.k8s_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"
  haystack_cluster_name = "${var.haystack_cluster_name}"

  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"

  firehose_writer_firehose_streamname = "${var.pipes_firehose_writer_firehose_streamname}"

  pipes = "${var.pipes}"
}

module "collectors" {
  source = "github.com/ExpediaDotCom/haystack-collector/deployment/terraform"

  app_namespace = "${var.k8s_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"
  haystack_cluster_name = "${var.haystack_cluster_name}"

  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  kinesis-stream_name = "${var.kinesis-stream_name}"
  kinesis-stream_region = "${var.kinesis-stream_region}"

  collector = "${var.collector}"
}

module "service-graph" {
  source = "github.com/ExpediaDotCom/haystack-service-graph/deployment/terraform"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  kafka_port = "${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  service-graph = "${var.service-graph}"
}

module "ui" {
  source = "github.com/ExpediaDotCom/haystack-ui/deployment/terraform"
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
  encoder_type = "${var.ui["encoder_type"]}"
}

module "haystack-console" {
  source = "github.com/ExpediaDotCom/haystack-console/deployment/terraform"
  image = "expediadotcom/haystack-console:${var.haystack-console["version"]}"
  haystack_domain_name="${var.domain_name}"
  haystack_cluster_name= "${var.haystack_cluster_name}"
  attributor_endpoint = "${var.haystack-console["console_attributor_endpoint"]}"
  influxdb_endpoint_host = "${var.haystack-console["influxdb_endpoint_host"]}"
  influxdb_endpoint_port = "${var.haystack-console["influxdb_endpoint_port"]}"
  grafana_endpoint = "${var.haystack-console["console_grafana_endpoint"]}"
  healthcheckthreshold_trends_iteratorAgeSeconds = "${var.haystack-console["healthcheckthreshold_trends_iteratorAgeSeconds"]}"
  healthcheckthreshold_traces_iteratorAgeSeconds = "${var.haystack-console["healthcheckthreshold_traces_iteratorAgeSeconds"]}"
  healthcheckthreshold_service-graph_iteratorAgeSeconds = "${var.haystack-console["healthcheckthreshold_service-graph_iteratorAgeSeconds"]}"
  healthcheckthreshold_collector_iteratorAgeSeconds = "${var.haystack-console["healthcheckthreshold_collector_iteratorAgeSeconds"]}"
  attributorAdditionalTags= "${var.haystack-console["attributorAdditionalTags"]}"
  replicas = "${var.haystack-console["console_instances"]}"
  namespace = "${var.k8s_app_namespace}"
  enabled = "${var.haystack-console["enabled"]}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.haystack-console["console_cpu_limit"]}"
  cpu_request = "${var.haystack-console["console_cpu_request"]}"
  memory_limit = "${var.haystack-console["console_memory_limit"]}"
  memory_request = "${var.haystack-console["console_memory_request"]}"
  jvm_memory_limit = "${var.haystack-console["console_jvm_memory_limit"]}"
  env_vars = "${var.haystack-console["console_environment_overrides"]}"
}

#Adaptive Alerting
module "alerting" {
  source = "github.com/ExpediaDotCom/adaptive-alerting/deployment/terraform"
#  source = "../../../../../../../aa/adaptive-alerting/deployment/terraform"

  app_namespace = "${var.aa_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"
  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"

  # AA apps
  alerting = "${var.alerting}"
  modelservice = "${var.modelservice}"
  ad-mapper = "${var.ad-mapper}"
  ad-manager = "${var.ad-manager}"
  visualizer = "${var.visualizer}"
  aa-metric-functions = "${var.aa-metric-functions}"
}

module "alert-manager" {
  source = "github.com/ExpediaDotCom/alert-manager/deployment/terraform"

  app_namespace = "${var.aa_app_namespace}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  node_selector_label = "${var.app-node_selector_label}"
  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"

  # AM apps
  alert-manager-service = "${var.alert-manager-service}"
  alert-manager-store = "${var.alert-manager-store}"
  alert-manager-notifier = "${var.alert-manager-notifier}"
}

module "haystack-alerting" {
  source = "github.com/ExpediaDotCom/haystack-alerting/deployment/terraform"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  kafka_port = "${var.kafka_port}"
  elasticsearch_port = "${var.elasticsearch_port}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  haystack-alerts = "${var.haystack-alerts}"
}

# Pitchfork
module "pitchfork" {
  source = "pitchfork"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  kafka_port = "${var.kafka_port}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  pitchfork = "${var.pitchfork}"
  domain_name = "${var.domain_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  node_selector_label = "${var.app-node_selector_label}"
}

module "haystack-agent" {
  source = "github.com/ExpediaDotCom/haystack-agent/deployment/terraform"
  haystack-agent = "${var.haystack-agent}"
  namespace = "${var.k8s_app_namespace}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kafka_port = "${var.kafka_port}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
}

module "reverse-proxy" {
  source = "github.com/ExpediaDotCom/blobs/deployment/terraform"
  reverse-proxy = "${var.reverse-proxy}"
  namespace = "${var.k8s_app_namespace}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  grpc_server_endpoint ="${module.haystack-agent.proxy_grpc_server_endpoint}"
}

module "haystack-attribution" {
  source = "github.com/ExpediaDotCom/haystack-attribution/deployment/terraform"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  kafka_port = "${var.kafka_port}"
  elasticsearch_port = "${var.elasticsearch_port}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  node_selector_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  haystack-attribution = "${var.haystack-attribution}"
  haystack_cluster_name ="${var.haystack_cluster_name}"
  haystack_domain_name="${var.domain_name}"
}
