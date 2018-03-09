locals {
  app_name = "haystack-ui"
  config_file_path = "${path.module}/templates/haystack-ui_json.tpl"
  container_config_path = "/config/haystack-ui.json"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "ui-${local.checksum}"
  trends_connector_name = "trends-connector-js"
}


resource "kubernetes_config_map" "haystack-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "haystack-ui.json" = "${data.template_file.config_data.rendered}"
  }
}

resource "kubernetes_config_map" "trends_connector" {
  metadata {
    name = "${local.trends_connector_name}"
    namespace = "${var.namespace}"
  }
  data {
    "trendsConnector.js" = "${file("${path.module}/templates/trendsConnector.js")}"
  }
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    trace_reader_hostname = "${var.trace_reader_hostname}"
    trace_reader_service_port = "${var.trace_reader_service_port}"
    metrictank_hostname = "${var.metrictank_hostname}"
    metrictank_port = "${var.metrictank_port}"
    graphite_port = "${var.graphite_port}"
    graphite_hostname = "${var.graphite_hostname}"
    whitelisted_fields = "${var.whitelisted_fields}"
  }
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    node_selecter_label = "${var.node_selecter_label}"
    image = "${var.image}"
    replicas = "${var.replicas}"
    memory_limit = "${var.memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    service_port = "${var.service_port}"
    container_port = "${var.container_port}"
    configmap_name = "${local.configmap_name}"
    trends_connector_name = "${local.trends_connector_name}"
  }
}

resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
}


resource "null_resource" "kubectl_destroy" {

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
}
