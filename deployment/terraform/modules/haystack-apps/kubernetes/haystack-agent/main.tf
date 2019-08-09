locals {
  app_name = "haystack-agent"
  deployment_yaml_file_path = "${path.module}/templates/deployment.yaml"
  count = "${var.haystack-agent["enabled"]?1:0}"
  config_file_path = "${path.module}/templates/haystack-agent.conf"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "haystack-agent-${local.checksum}"
}

resource "kubernetes_config_map" "haystack-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "haystack-agent.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
    aws_bucket_name = "${var.haystack-agent["blobs_aws_bucket_name"]}"
    aws_region = "${var.haystack-agent["blobs_aws_region"]}"
  }
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"

  vars {
    app_name = "${local.app_name}"
    node_selecter_label = "${var.node_selector_label}"
    image = "expediadotcom/haystack-agent:${var.haystack-agent["version"]}"
    replicas = "${var.haystack-agent["instances"]}"
    enabled = "${var.haystack-agent["enabled"]}"
    cpu_limit = "${var.haystack-agent["cpu_limit"]}"
    cpu_request = "${var.haystack-agent["cpu_request"]}"
    memory_limit = "${var.haystack-agent["memory_limit"]}"
    memory_request = "${var.haystack-agent["memory_request"]}"
    jvm_memory_limit = "${var.haystack-agent["jvm_memory_limit"]}"
    kubectl_context_name = "${var.kubectl_context_name}"
    kubectl_executable_name = "${var.kubectl_executable_name}"
    namespace = "${var.namespace}"
    spans_service_port = "${var.spans_service_port}"
    blobs_service_port = "${var.blobs_service_port}"
    configmap_name = "${local.configmap_name}"
    graphite_port = "${var.graphite_port}"
    graphite_host = "${var.graphite_hostname}"
    graphite_enabled = "${var.graphite_enabled}"
  }
}

resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  count = "${local.count}"
}


resource "null_resource" "kubectl_destroy" {

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
}