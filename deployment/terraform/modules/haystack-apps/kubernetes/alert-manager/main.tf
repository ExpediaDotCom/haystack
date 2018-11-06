locals {
  kafka_endpoint =  "${var.kafka_hostname}:${var.kafka_port}"
}

# ========================================
# Alert Manager
# ========================================

module "alert-manager" {
  source = "github.com/ExpediaDotCom/alert-manager/deployment/terraform/deprecated-alert-manager"

  # Docker
  image = "${var.alert-manager["image"]}"
  image_pull_policy = "${var.alert-manager["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.alert-manager["enabled"]}"
  replicas = "${var.alert-manager["instances"]}"
  cpu_limit = "${var.alert-manager["cpu_limit"]}"
  cpu_request = "${var.alert-manager["cpu_request"]}"
  memory_limit = "${var.alert-manager["memory_limit"]}"
  memory_request = "${var.alert-manager["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.alert-manager["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  env_vars = "${var.alert-manager["environment_overrides"]}"

  # App
  db_endpoint = "${var.alert-manager["db_endpoint"]}"
  smtp_host = "${var.alert-manager["smtp_host"]}"
  mail_from = "${var.alert-manager["mail_from"]}"
}

module "alert-manager-api" {
  source = "github.com/ExpediaDotCom/alert-manager/deployment/terraform/alert-manager-api"

  # Docker
  image = "${var.alert-manager-api["image"]}"
  image_pull_policy = "${var.alert-manager-api["image_pull_policy"]}"

  # Kubernetes
  namespace = "${var.app_namespace}"
  enabled = "${var.alert-manager-api["enabled"]}"
  replicas = "${var.alert-manager-api["instances"]}"
  cpu_limit = "${var.alert-manager-api["cpu_limit"]}"
  cpu_request = "${var.alert-manager-api["cpu_request"]}"
  memory_limit = "${var.alert-manager-api["memory_limit"]}"
  memory_request = "${var.alert-manager-api["memory_request"]}"
  node_selector_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"

  # Environment
  jvm_memory_limit = "${var.alert-manager-api["jvm_memory_limit"]}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  env_vars = "${var.alert-manager-api["environment_overrides"]}"
}
