locals {
  kafka_endpoint =  "${var.kafka_hostname}:${var.kafka_port}"
}

module "anomaly-validator" {
  source = "anomaly-validator"

  image = "expediadotcom/haystack-adaptive-alerting-anomaly-validator:${var.alerting["version"]}"
  replicas = "${var.anomaly-validator["anomaly_validator_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${local.kafka_endpoint}"
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

module "ad-mapper" {
  source = "ad-mapper"

  image = "expediadotcom/haystack-adaptive-alerting-ad-mapper:${var.alerting["version"]}"
  replicas = "${var.ad-mapper["ad_mapper_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${local.kafka_endpoint}"
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
  kafka_endpoint = "${local.kafka_endpoint}"
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

module "modelservice" {
  source = "modelservice"

  image = "expediadotcom/haystack-adaptive-alerting-modelservice:${var.alerting["version"]}"
  replicas = "${var.modelservice["modelservice_instances"]}"
  namespace = "${var.app_namespace}"
  db_endpoint = "${var.modelservice["modelservice_db_endpoint"]}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.modelservice["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.modelservice["modelservice_cpu_limit"]}"
  cpu_request = "${var.modelservice["modelservice_cpu_request"]}"
  memory_limit = "${var.modelservice["modelservice_memory_limit"]}"
  memory_request = "${var.modelservice["modelservice_memory_request"]}"
  jvm_memory_limit = "${var.modelservice["modelservice_jvm_memory_limit"]}"
  env_vars = "${var.modelservice["modelservice_environment_overrides"]}"
}

module "aquila-trainer" {
  source = "aquila-trainer"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.aquila-trainer["enabled"]}"
  replicas = "${var.aquila-trainer["instances"]}"
  cpu_limit = "${var.aquila-trainer["cpu_limit"]}"
  cpu_request = "${var.aquila-trainer["cpu_request"]}"
  memory_limit = "${var.aquila-trainer["memory_limit"]}"
  memory_request = "${var.aquila-trainer["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Docker
  image = "${var.aquila-trainer["image"]}"
  image_pull_policy = "${var.aquila-trainer["image_pull_policy"]}"

  # Environment
  jvm_memory_limit = "${var.aquila-trainer["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  env_vars = "${var.aquila-trainer["environment_overrides"]}"
}

module "aquila-detector" {
  source = "aquila-detector"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.aquila-detector["enabled"]}"
  replicas = "${var.aquila-detector["instances"]}"
  cpu_limit = "${var.aquila-detector["cpu_limit"]}"
  cpu_request = "${var.aquila-detector["cpu_request"]}"
  memory_limit = "${var.aquila-detector["memory_limit"]}"
  memory_request = "${var.aquila-detector["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Docker
  image = "${var.aquila-detector["image"]}"
  image_pull_policy = "${var.aquila-detector["image_pull_policy"]}"

  # Environment
  jvm_memory_limit = "${var.aquila-detector["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  env_vars = "${var.aquila-detector["environment_overrides"]}"
}
