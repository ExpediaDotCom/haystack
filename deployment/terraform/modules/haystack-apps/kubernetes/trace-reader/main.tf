locals {
  app_name = "trace-reader"
  config_file_path = "${path.module}/config/trace-reader_conf.tpl"
  container_config_path = "/config/trace-reader.conf"
  count = "${var.enabled?1:0}"
}


resource "kubernetes_service" "haystack-service" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }
  spec {
    selector {
      app = "${kubernetes_replication_controller.haystack-trace-reader-rc.metadata.0.labels.app}"
    }
    port {
      port = "${var.service_port}"
      target_port = "${var.container_port}"
    }
  }
  count = "${local.count}"
}

data "template_file" "haystck_trace_reader_config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    elasticsearch_endpoint = "${var.elasticsearch_endpoint}"
    cassandra_hostname = "${var.cassandra_hostname}"
  }
}

resource "kubernetes_config_map" "haystack-trace-reader" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }

  data {
    "trace-indexer.conf" = "${data.template_file.haystck_trace_reader_config_data.rendered}"
  }
}

resource "kubernetes_replication_controller" "haystack-trace-reader-rc" {
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
          name = "${kubernetes_config_map.haystack-trace-reader.metadata.0.name}"
        }
      }
    }

    "selector" {
      app = "${local.app_name}"
    }
  }
  count = "${local.count}"
}