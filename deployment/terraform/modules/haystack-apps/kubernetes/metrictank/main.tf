locals {
  app_name = "metrictank"
  service_port = 6060
  container_port = 6060
  image = "raintank/metrictank:latest"
  count = "${var.enabled?1:0}"

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
          name = "MT_HTTP_MULTI_TENANT"
          value = "false"
        }
        env {
          name = "MT_CARBON_IN_ENABLED"
          value = "false"
        }
        env {
          name = "MT_KAFKA_MDM_IN_ENABLED"
          value = "true"
        }
        env {
          name = "MT_CASSANDRA_ADDRS"
          value = "${var.cassandra_address}"
        }
        env {
          name = "MT_KAFKA_MDM_IN_BROKERS"
          value = "${var.kafka_address}"
        }
        env {
          name = "MT_CASSANDRA_IDX_HOSTS"
          value = "${var.cassandra_address}"
        }
        env {
          name = "MT_STATS_ADDR"
          value = "${var.graphite_address}"
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
    }

    "selector" {
      app = "${local.app_name}"
    }
  }
}