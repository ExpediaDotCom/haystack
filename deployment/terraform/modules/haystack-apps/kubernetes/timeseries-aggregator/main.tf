locals {
  app_name = "timeseries-aggregator"
  config_file_path = "${path.module}/config/timeseries-aggregator_conf.tpl"
  container_config_path = "/config/timeseries-aggregator.conf"
  count = "${var.enabled?1:0}"
}

data "template_file" "haystck_timeseries_aggregator_config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    kafka_endpoint = "${var.kafka_endpoint}"
  }
}

resource "kubernetes_config_map" "haystack-timeseries-aggregator" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }

  data {
    "timeseries-aggregator.conf" = "${data.template_file.haystck_timeseries_aggregator_config_data.rendered}"
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
        image = "${var.image}"
        name = "${local.app_name}"
        env {
          name = "HAYSTACK_OVERRIDES_CONFIG_PATH"
          value = "${local.container_config_path}"
        }
        env {
          name = "HAYSTACK_GRAPHITE_HOST"
          value = "${var.graphite_hostname}"
        }
        env {
          name = "HAYSTACK_GRAPHITE_PORT"
          value = "${var.graphite_port}"
        }
        volume_mount {
          mount_path = "/config"
          name = "config-volume"
        }
        liveness_probe {
          initial_delay_seconds = 15
          failure_threshold = 2
          period_seconds = 5
          exec {
            command = [
              "grep",
              "true",
              "/app/isHealthy"]
          }
        }
        resources {
          limits {
            memory = "1500Mi"
          }
          requests {
            cpu = "500m"
            memory = "1500Mi"
          }
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
      volume {
        name = "config-volume"
        config_map {
          name = "${kubernetes_config_map.haystack-timeseries-aggregator.metadata.0.name}"
        }
      }
      node_selector = "${var.node_selecter_label}"
    }
    "selector" {
      app = "${local.app_name}"
    }
  }
  count = "${local.count}"
}
