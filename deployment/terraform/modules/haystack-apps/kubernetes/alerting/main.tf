locals {
  external_metric_tank_enabled = "${var.metrictank["external_hostname"] != "" && var.metrictank["external_kafka_broker_hostname"] != ""? "true" : "false"}"
  internal_kafka_endpoint =  "${var.kafka_hostname}:${var.kafka_port}"
  external_kafka_endpoint = "${var.metrictank["external_kafka_broker_hostname"]}:${var.metrictank["external_kafka_broker_port"]}"
}

module "ad-mapper" {
  source = "ad-mapper"

  image = "expediadotcom/haystack-adaptive-alerting-ad-mapper:${var.alerting["version"]}"
  replicas = "${var.ad-mapper["ad_mapper_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.ad-mapper["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.ad-mapper["ad_mapper_cpu_limit"]}"
  cpu_request = "${var.ad-mapper["ad_mapper_cpu_request"]}"
  memory_limit = "${var.ad-mapper["ad_mapper_memory_limit"]}"
  memory_request = "${var.ad-mapper["ad_mapper_memory_request"]}"
  jvm_memory_limit = "${var.ad-mapper["ad_mapper_jvm_memory_limit"]}"
  env_vars = "${var.ad-mapper["ad_mapper_environment_overrides"]}"
}

module "ad-manager" {
  source = "ad-manager"

  image = "expediadotcom/haystack-adaptive-alerting-ad-manager:${var.alerting["version"]}"
  replicas = "${var.ad-manager["ad_manager_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.ad-manager["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.ad-manager["ad_manager_cpu_limit"]}"
  cpu_request = "${var.ad-manager["ad_manager_cpu_request"]}"
  memory_limit = "${var.ad-manager["ad_manager_memory_limit"]}"
  memory_request = "${var.ad-manager["ad_manager_memory_request"]}"
  jvm_memory_limit = "${var.ad-manager["ad_manager_jvm_memory_limit"]}"
  env_vars = "${var.ad-manager["ad_manager_environment_overrides"]}"
}

module "anomaly-validator" {
  source = "anomaly-validator"

  image = "expediadotcom/haystack-adaptive-alerting-anomaly-validator:${var.alerting["version"]}"
  replicas = "${var.anomaly-validator["anomaly_validator_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.anomaly-validator["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.anomaly-validator["anomaly_validator_cpu_limit"]}"
  cpu_request = "${var.anomaly-validator["anomaly_validator_cpu_request"]}"
  memory_limit = "${var.anomaly-validator["anomaly_validator_memory_limit"]}"
  memory_request = "${var.anomaly-validator["anomaly_validator_memory_request"]}"
  jvm_memory_limit = "${var.anomaly-validator["anomaly_validator_jvm_memory_limit"]}"
  env_vars = "${var.anomaly-validator["anomaly_validator_environment_overrides"]}"
  investigation_endpoint = "${var.anomaly-validator["anomaly_validator_investigation_endpoint"]}"
}

module "aquila-trainer" {
  source = "anomaly-validator"

  image = "expediadotcom/aquila-trainer:${var.alerting["version"]}"
  replicas = "${var.aquila-trainer["aquila_trainer_instances"]}"
  namespace = "${var.app_namespace}"

  # FIXME This shouldn't be required as Aquila Trainer isn't a Kafka app. [WLW]
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"

  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.anomaly-validator["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.aquila-trainer["aquila_trainer_cpu_limit"]}"
  cpu_request = "${var.aquila-trainer["aquila_trainer_cpu_request"]}"
  memory_limit = "${var.aquila-trainer["aquila_trainer_memory_limit"]}"
  memory_request = "${var.aquila-trainer["aquila_trainer_memory_request"]}"
  jvm_memory_limit = "${var.aquila-trainer["aquila_trainer_jvm_memory_limit"]}"
  env_vars = "${var.aquila-trainer["aquila_trainer_environment_overrides"]}"

  # TODO Replace with aquila-trainer equivalents. [WLW]
#  investigation_endpoint = "${var.anomaly-validator["anomaly_validator_investigation_endpoint"]}"
}


# ========================================
# Deprecated modules
# ========================================

module "metric-router" {
  source = "metric-router"
  image = "expediadotcom/haystack-adaptive-alerting-metric-router:${var.alerting["version"]}"
  replicas = "${var.metric-router["metric_router_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${local.internal_kafka_endpoint}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.metric-router["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.metric-router["metric_router_cpu_limit"]}"
  cpu_request = "${var.metric-router["metric_router_cpu_request"]}"
  memory_limit = "${var.metric-router["metric_router_memory_limit"]}"
  memory_request = "${var.metric-router["metric_router_memory_request"]}"
  jvm_memory_limit = "${var.metric-router["metric_router_jvm_memory_limit"]}"
  env_vars = "${var.metric-router["metric_router_environment_overrides"]}"
}

module "ewma-detector" {
  source = "ewma-detector"
  image = "expediadotcom/haystack-adaptive-alerting-ewma-detector:${var.alerting["version"]}"
  replicas = "${var.ewma-detector["ewma_detector_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.ewma-detector["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.ewma-detector["ewma_detector_cpu_limit"]}"
  cpu_request = "${var.ewma-detector["ewma_detector_cpu_request"]}"
  memory_limit = "${var.ewma-detector["ewma_detector_memory_limit"]}"
  memory_request = "${var.ewma-detector["ewma_detector_memory_request"]}"
  jvm_memory_limit = "${var.ewma-detector["ewma_detector_jvm_memory_limit"]}"
  env_vars = "${var.ewma-detector["ewma_detector_environment_overrides"]}"
}

module "constant-detector" {
  source = "constant-detector"

  image = "expediadotcom/haystack-adaptive-alerting-constant-detector:${var.alerting["version"]}"
  replicas = "${var.constant-detector["constant_detector_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.constant-detector["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.constant-detector["constant_detector_cpu_limit"]}"
  cpu_request = "${var.constant-detector["constant_detector_cpu_request"]}"
  memory_limit = "${var.constant-detector["constant_detector_memory_limit"]}"
  memory_request = "${var.constant-detector["constant_detector_memory_request"]}"
  jvm_memory_limit = "${var.constant-detector["constant_detector_jvm_memory_limit"]}"
  env_vars = "${var.constant-detector["constant_detector_environment_overrides"]}"
}

module "pewma-detector" {
  source = "pewma-detector"

  image = "expediadotcom/haystack-adaptive-alerting-pewma-detector:${var.alerting["version"]}"
  replicas = "${var.pewma-detector["pewma_detector_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.pewma-detector["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.pewma-detector["pewma_detector_cpu_limit"]}"
  cpu_request = "${var.pewma-detector["pewma_detector_cpu_request"]}"
  memory_limit = "${var.pewma-detector["pewma_detector_memory_limit"]}"
  memory_request = "${var.pewma-detector["pewma_detector_memory_request"]}"
  jvm_memory_limit = "${var.pewma-detector["pewma_detector_jvm_memory_limit"]}"
  env_vars = "${var.pewma-detector["pewma_detector_environment_overrides"]}"
}

