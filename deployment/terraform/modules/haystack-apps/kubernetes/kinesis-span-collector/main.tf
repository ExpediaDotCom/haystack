locals {
  app_name = "kinesis-span-collector"
  config_file_path = "${path.module}/config/kinesis-span-collector_conf.tpl"
  container_config_path = "/config/kinesis-span-collector.conf"
  count = "${var.enabled?1:0}"
}

data "template_file" "haystck_kinesis_span_collector_config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    kinesis_stream_region = "${var.kinesis_stream_region}"
    kinesis_stream_name = "${var.kinesis_stream_name}"
    kafka_endpoint = "${var.kafka_endpoint}"
    sts_role_arn = "${var.sts_role_arn}"
    haystack_cluster_name = "${var.haystack_cluster_name}"
  }
}

resource "kubernetes_config_map" "haystack-kinesis-span-collector" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }

  data {
    "kinesis-span-collector.conf" = "${data.template_file.haystck_kinesis_span_collector_config_data.rendered}"
  }
}

resource "kubernetes_replication_controller" "haystack-kinesis-span-collector-rc" {
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
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
      volume {
        name = "config-volume"
        config_map {
          name = "${kubernetes_config_map.haystack-kinesis-span-collector.metadata.0.name}"
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