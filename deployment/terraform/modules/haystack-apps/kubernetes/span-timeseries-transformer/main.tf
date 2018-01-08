locals {
  app_name = "span-timeseries-transformer"
  config_file_path = "${path.module}/config/span-timeseries-transformer_conf.tpl"
  container_config_path = "/config/span-timeseries-transformer.conf"
  count = "${var.enabled?1:0}"
}

data "template_file" "haystck_span_timeseries_transformer_config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    kafka_endpoint = "${var.kafka_endpoint}"
  }
}

resource "kubernetes_config_map" "haystack-span-timeseries-transformer" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }

  data {
    "span-timeseries-transformer.conf" = "${data.template_file.haystck_span_timeseries_transformer_config_data.rendered}"
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
        volume_mount {
          mount_path = "/config"
          name = "config-volume"
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
      volume {
        name = "config-volume"
        config_map {
          name = "${kubernetes_config_map.haystack-span-timeseries-transformer.metadata.0.name}"
        }
      }
    }

    "selector" {
      app = "${local.app_name}"
    }
  }
  count = "${local.count}"
}