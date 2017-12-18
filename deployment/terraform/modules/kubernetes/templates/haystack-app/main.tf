resource "kubernetes_service" "haystack-service" {
  metadata {
    name = "${var.app_name}-service"
    namespace = "${var.namespace}"
  }
  spec {
    selector {
      app = "${kubernetes_replication_controller.haystack-rc.metadata.0.labels.app}"
    }
    port {
      port = "${var.service_port}"
      target_port = "${var.container_port}"
    }
  }
  count = "${var.create_service && var.enabled?1:0}"
}

resource "kubernetes_replication_controller" "haystack-rc" {
  metadata {
    name = "${var.app_name}-rc"
    labels {
      app = "${var.app_name}"
    }
    namespace = "${var.namespace}"
  }
  "spec" {
    replicas = "${var.replicas}"
    template {
      container {
        image = "${var.image}"
        name = "${var.app_name}"
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
    }
    "selector" {
      app = "${var.app_name}"
    }
  }
  count = "${var.enabled?1:0}"
}