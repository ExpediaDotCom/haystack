locals {
  app_name = "zookeeper"
  service_port = 2181
  container_port = 2181
  image = "wurstmeister/zookeeper:3.4.6"
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
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
    }

    "selector" {
      app = "${local.app_name}"
    }
  }
  count = "${var.enabled?1:0}"

}