locals {
  app_name = "haystack-ui"
  config_file_path = "${path.module}/config/haystack-ui_json.tpl"
  container_config_path = "/config/haystack-ui.json"
}

data "template_file" "haystck_ui_config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    trace_reader_hostname = "${var.trace_reader_hostname}"
    trace_reader_service_port = "${var.trace_reader_service_port}"
    metrictank_hostname = "${var.metrictank_hostname}"
    metrictank_port = "${var.metrictank_port}"
  }
}

resource "kubernetes_config_map" "haystack-ui" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }

  data {
    "haystack-ui.json" = "${data.template_file.haystck_ui_config_data.rendered}"
  }
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
      port = "${var.service_port}"
      target_port = "${var.container_port}"
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
      node_selector = "${var.node_selecter_label}"


      termination_grace_period_seconds = "${var.termination_grace_period}"
      volume {
        name = "config-volume"
        config_map {
          name = "${kubernetes_config_map.haystack-ui.metadata.0.name}"
        }
      }
    }
    "selector" {
      app = "${local.app_name}"
    }
  }
}