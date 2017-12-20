locals {
  app_name = "haystack-ui"
  config_file_path = "${path.module}/config/haystack-ui.json"
  container_config_path = "/config/haystack-ui.json"
}

resource "kubernetes_config_map" "haystack-ui" {
  metadata {
    name = "${local.app_name}"
    namespace = "${var.namespace}"
  }

  data {
    "haystack-ui.json" = "${file("${local.config_file_path}")}"
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