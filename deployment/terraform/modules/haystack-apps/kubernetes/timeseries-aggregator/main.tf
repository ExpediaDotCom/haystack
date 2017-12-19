locals {
  app_name = "timeseries-aggregator"
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
        image = "${var.image}"
        name = "${local.app_name}"
        env {
          name = "HAYSTACK_PROP_KAFKA_STREAMS_BOOTSTRAP_SERVERS"
          value = "${var.kafka_endpoint}"
        }
        liveness_probe {
          initial_delay_seconds = 15
          period_seconds = 5
          failure_threshold = 2
          exec {
            command = [
              "grep",
              "true",
              "/app/isHealthy"]
          }
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
    }
    "selector" {
      app = "${local.app_name}"
    }
  }
}