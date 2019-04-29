//when running locally we expect the machine to have a local k8s cluster using minikube
locals {
  k8s_datastores_heap_memory_in_mb = "1024"
  graphite_hostname = "monitoring-influxdb-graphite.kube-system.svc"
  graphite_port = 2003
}

module "k8s-addons" {
  source = "../../../modules/k8s-addons"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  datastores_heap_memory_in_mb = "${local.k8s_datastores_heap_memory_in_mb}"
  aa_apps_resource_limits = "${var.aa_apps_resource_limits}"
  monitoring_addons = "${var.monitoring_addons}"
  alerting_addons = "${var.alerting_addons}"
  logging_addons = "${var.logging_addons}"
  cluster = "${var.cluster}"
}
