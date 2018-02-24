locals {
  app_name = "pipes-kafka-producer"
  count = "${var.enabled?1:0}"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"

}


data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    gra = "${var.graphite_address}"
    graphite_address = "${var.graphite_address}"
    node_selecter_label = "${var.node_selecter_label}"
    kafka_address = "${var.kafka_address}"
    cassandra_address = "${var.cassandra_address}"
    replicas = "${var.replicas}"
    memory_limit = "${var.memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    service_port = "${local.service_port}"
    container_port = "${local.container_port}"
  }
}


resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
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
          name = "HAYSTACK_KAFKA_BROKERS"
          value = "${var.kafka_hostname}"
        }
        env {
          name = "HAYSTACK_GRAPHITE_HOST"
          value = "${var.graphite_hostname}"
        }
        env {
          name = "HAYSTACK_GRAPHITE_PORT"
          value = "${var.graphite_port}"
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
      node_selector = "${var.node_selecter_label}"
    }

    "selector" {
      app = "${local.app_name}"
    }
  }
  count = "${local.count}"
}