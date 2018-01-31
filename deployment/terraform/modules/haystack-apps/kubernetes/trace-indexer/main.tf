locals {
  app_name = "trace-indexer"
  config_file_path = "${path.module}/config/trace-indexer_conf.tpl"
  container_config_path = "/config/trace-indexer.conf"
  count = "${var.enabled?1:0}"
  span_produce_topic = "${var.enable_kafka_sink?"span-buffer":""}"
}

data "template_file" "haystack_trace_indexer_config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    kafka_endpoint = "${var.kafka_endpoint}"
    elasticsearch_endpoint = "${var.elasticsearch_endpoint}"
    cassandra_hostname = "${var.cassandra_hostname}"
    span_produce_topic = "${local.span_produce_topic}"
  }
}

resource "kubernetes_config_map" "haystack-trace-indexer" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }

  data {
    "trace-indexer.conf" = "${data.template_file.haystack_trace_indexer_config_data.rendered}"
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
            command = ["grep","true","/app/isHealthy"]
          }
        }
      }
      termination_grace_period_seconds = "${var.termination_grace_period}"
      volume {
        name = "config-volume"
        config_map {
          name = "${kubernetes_config_map.haystack-trace-indexer.metadata.0.name}"
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