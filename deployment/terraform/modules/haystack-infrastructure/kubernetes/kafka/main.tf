locals {
  app_name = "kafka-service"
  service_port = 9092
  container_port = 9092
  image = "wurstmeister/kafka:0.10.2.1"
  topics = "proto-spans:1:1,metricpoints:1:1,mdm:1:1,span-buffer:1:1"
  host_name = "192.168.99.100"
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
        image = "${local.image}"
        name = "${local.app_name}"
        env {
          name = "KAFKA_ADVERTISED_HOST_NAME"
          value = "${local.host_name}"
        }
        env {
          name = "KAFKA_ADVERTISED_PORT"
          value = "${local.service_port}"
        }
        env {
          name = "KAFKA_ZOOKEEPER_CONNECT"
          value = "${var.zk_connection_string}"
        }
        env {
          name = "KAFKA_CREATE_TOPICS"
          value = "${local.topics}"
        }
        port {
          container_port = 9092
          host_port = 9092
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
    }

    "selector" {
      app = "${local.app_name}"
    }
  }
}