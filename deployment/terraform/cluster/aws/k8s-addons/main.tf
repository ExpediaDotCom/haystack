locals {
  k8s_datastores_heap_memory_in_mb = "2048"
  k8s_cluster_name = "${var.cluster["name"]}-k8s.${var.cluster["domain_name"]}"
}

module "k8s-addons" {
  source = "../../../modules/k8s-addons"
  kubectl_context_name = "${local.k8s_cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  datastores_heap_memory_in_mb = "${local.k8s_datastores_heap_memory_in_mb}"
  aa_apps_resource_limits = "${var.aa_apps_resource_limits}"
  monitoring_addons = "${var.monitoring_addons}"
  alerting_addons = "${var.alerting_addons}"
  logging_addons = "${var.logging_addons}"
  cluster = "${var.cluster}"
}
