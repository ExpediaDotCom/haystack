resource "null_resource" "wait_for_dns_propogation" {
  provisioner "local-exec" {
    command = "for i in {1..50}; do ${var.kubectl_executable_name} get nodes --context ${var.k8s_cluster_name} -- && break || sleep 15; done"
  }
}

resource "kubernetes_namespace" "haystack-app-namespace" {
  metadata {
    name = "${var.k8s_app_namespace}"
    annotations {
      cluster_name = "${var.k8s_cluster_name}"
    }

  }
  depends_on = [
    "null_resource.wait_for_dns_propogation"]
}

module "monitoring-addons" {
  source = "monitoring"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.add_monitoring_addons}"
  k8s_cluster_name = "${kubernetes_namespace.haystack-app-namespace.metadata.0.annotations.cluster_name}"
  grafana_storage_volume = "${var.grafana_storage_volume}"
  k8s_storage_class = "${var.k8s_storage_class}"
  influxdb_storage_volume = "${var.influxdb_storage_volume}"
}

module "logging-addongs" {
  source = "logging"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_cluster_name = "${kubernetes_namespace.haystack-app-namespace.metadata.0.annotations.cluster_name}"
  enabled = "${var.add_logging_addons}"
  container_log_path = "${var.container_log_path}"
  es_nodes = "${var.logging_es_nodes}"
  k8s_storage_class = "${var.k8s_storage_class}"
  es_storage_volume = "${var.es_storage_volume}"
}

module "traefik-addon" {
  source = "traefik"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_app_namespace = "${kubernetes_namespace.haystack-app-namespace.metadata.0.name}"
  haystack_domain_name = "${var.haystack_domain_name}"
  traefik_node_port = "${var.traefik_node_port}"
  k8s_cluster_name = "${kubernetes_namespace.haystack-app-namespace.metadata.0.annotations.cluster_name}"
}
