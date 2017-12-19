locals {
  app_name = "cassandra"
  service_port = 9042
  container_port = 9042
  image = "cassandra:3.11.0"
}

resource "kubernetes_service" "haystack-service" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }
  spec {
    selector {
      app = "${kubernetes_replication_controller.haystack-rc.metadata.0.labels.app}"
    }
    port {
      port = "${local.service_port}"
      target_port = "${local.container_port}"
    }
  }
  count = "${var.enabled?1:0}"

}


resource "kubernetes_replication_controller" "haystack-rc" {
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
        image = "${local.image}"
        name = "${local.app_name}"
        env {
          name = "MAX_HEAP_SIZE"
          value = "256m"
        }
        env {
          name = "HEAP_NEWSIZE"
          value = "256m"
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
    }
    "selector" {
      app = "${local.app_name}"
    }
  }
  count = "${var.enabled?1:0}"
}