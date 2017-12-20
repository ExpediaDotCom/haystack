locals {
  app_name = "elasticsearch"
  service_port = 9200
  container_port = 9200
  es_docker_image = "elasticsearch:5-alpine"
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
        image = "${local.es_docker_image}"
        name = "${local.app_name}"
        env {
          name = "ES_JAVA_OPTS"
          value = "-Xms256m -Xmx256m"
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
    }

    "selector" {
      app = "${local.app_name}"
    }
  }
}