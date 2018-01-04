locals {
  app_name = "trace-reader"
  count = "${var.enabled?1:0}"
}


resource "kubernetes_service" "haystack-service" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }
  spec {
    selector {
      app = "${kubernetes_replication_controller.haystack-trace-reader-rc.metadata.0.labels.app}"
    }
    port {
      port = "${var.service_port}"
      target_port = "${var.container_port}"
    }
  }
  count = "${local.count}"
}

resource "kubernetes_replication_controller" "haystack-trace-reader-rc" {
  metadata {
    name = "${local.app_name}"
    labels {
      app = "${local.app_name}"
    }
    namespace = "${var.namespace}"
  }
  "spec" {
    replicas = "${var.replicas}"
    template {
      container {
        image = "${var.image}"
        name = "${local.app_name}"
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"

    }

    "selector" {
      app = "${local.app_name}"
    }
  }
  count = "${local.count}"
}