resource "null_resource" "kubectl_namespace" {
    provisioner "local-exec" {
    command = "${var.kubectl_executable_name} config use-context ${var.k8s_cluster_name}"
  }
}
resource "kubernetes_namespace" "haystack-app-namespace" {
  metadata {
    name = "${var.k8s_app_namespace}"
  }
}

module "monitoring-addons" {
  source = "monitoring"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.add_monitoring_addons}"
}

module "logging-addongs" {
  source = "logging"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  enabled = "${var.add_logging_addons}"
}

module "traefik-addon" {
  source = "traefik"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_app_namespace = "${var.k8s_app_namespace}"
  haystack_domain_name = "${var.haystack_domain_name}"
  traefik_node_port = "${var.traefik_node_port}"
}
