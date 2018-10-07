locals {
  kafka_endpoint =  "${var.kafka_hostname}:${var.kafka_port}"
}

# ========================================
# Adaptive Alerting
# ========================================

module "modelservice" {
  source = "modelservice"

  image = "expediadotcom/haystack-adaptive-alerting-modelservice:${var.alerting["version"]}"
  replicas = "${var.modelservice["instances"]}"
  namespace = "${var.app_namespace}"
  db_endpoint = "${var.modelservice["db_endpoint"]}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.modelservice["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.modelservice["cpu_limit"]}"
  cpu_request = "${var.modelservice["cpu_request"]}"
  memory_limit = "${var.modelservice["memory_limit"]}"
  memory_request = "${var.modelservice["memory_request"]}"
  jvm_memory_limit = "${var.modelservice["jvm_memory_limit"]}"
  env_vars = "${var.modelservice["environment_overrides"]}"
}

module "ad-mapper" {
  source = "ad-mapper"

  image = "expediadotcom/haystack-adaptive-alerting-ad-mapper:${var.alerting["version"]}"
  replicas = "${var.ad-mapper["instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${local.kafka_endpoint}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  modelservice_uri_template = "${var.ad-mapper["modelservice_uri_template"]}"
  enabled = "${var.ad-mapper["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.ad-mapper["cpu_limit"]}"
  cpu_request = "${var.ad-mapper["cpu_request"]}"
  memory_limit = "${var.ad-mapper["memory_limit"]}"
  memory_request = "${var.ad-mapper["memory_request"]}"
  jvm_memory_limit = "${var.ad-mapper["jvm_memory_limit"]}"
  env_vars = "${var.ad-mapper["environment_overrides"]}"
}

module "ad-manager" {
  source = "ad-manager"

  # Docker
  image = "${var.ad-manager["image"]}"
  image_pull_policy = "${var.ad-manager["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.ad-manager["enabled"]}"
  replicas = "${var.ad-manager["instances"]}"
  cpu_limit = "${var.ad-manager["cpu_limit"]}"
  cpu_request = "${var.ad-manager["cpu_request"]}"
  memory_limit = "${var.ad-manager["memory_limit"]}"
  memory_request = "${var.ad-manager["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.ad-manager["jvm_memory_limit"]}"
  graphite_enabled = "${var.graphite_enabled}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  env_vars = "${var.ad-manager["environment_overrides"]}"

  # App
  kafka_endpoint = "${local.kafka_endpoint}"
  aquila_uri = "${var.ad-manager["aquila_uri"]}"
  models_region = "${var.ad-manager["models_region"]}"
  models_bucket = "${var.ad-manager["models_bucket"]}"
}

module "anomaly-validator" {
  source = "anomaly-validator"

  image = "expediadotcom/haystack-adaptive-alerting-anomaly-validator:${var.alerting["version"]}"
  replicas = "${var.anomaly-validator["instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${local.kafka_endpoint}"
  graphite_hostname = "${var.graphite_hostname}"
  node_selecter_label = "${var.node_selector_label}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enabled = "${var.anomaly-validator["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.anomaly-validator["cpu_limit"]}"
  cpu_request = "${var.anomaly-validator["cpu_request"]}"
  memory_limit = "${var.anomaly-validator["memory_limit"]}"
  memory_request = "${var.anomaly-validator["memory_request"]}"
  jvm_memory_limit = "${var.anomaly-validator["jvm_memory_limit"]}"
  env_vars = "${var.anomaly-validator["environment_overrides"]}"
  investigation_endpoint = "${var.anomaly-validator["investigation_endpoint"]}"
}

# ========================================
# Aquila
# ========================================

# TODO Below we want to source external Aquila modules, but we can't because Trinity
# doesn't have git available on the path. Need to look into this. [WLW]
# https://trinity.tools.expedia.com/job/monitoring-test_haystack-deployment/1252/console
# Error downloading modules: Error loading modules: error downloading 'https://github.com/ExpediaDotCom/haystack-aquila.git?ref=v0.1.0': git must be available and on the PATH

module "aquila-detector" {
#  source = "github.com/ExpediaDotCom/haystack-aquila//detect/terraform/module?ref=v0.1.0"
  source = "aquila-detect"

  # Docker
  image = "${var.aquila-detector["image"]}"
  image_pull_policy = "${var.aquila-detector["image_pull_policy"]}"

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

  # Environment
  jvm_memory_limit = "${var.aquila-detector["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  env_vars = "${var.aquila-detector["environment_overrides"]}"
}

module "aquila-trainer" {
  source = "aquila-train"

  # Docker
  image = "${var.aquila-trainer["image"]}"
  image_pull_policy = "${var.aquila-trainer["image_pull_policy"]}"

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

  # Environment
  jvm_memory_limit = "${var.aquila-trainer["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  env_vars = "${var.aquila-trainer["environment_overrides"]}"
}
