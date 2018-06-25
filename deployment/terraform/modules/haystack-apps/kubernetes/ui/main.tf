locals {
  app_name = "haystack-ui"
  count = "${var.enabled?1:0}"
  config_file_path = "${path.module}/templates/haystack-ui_json.tpl"
  container_config_path = "/config/haystack-ui.json"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "ui-${local.checksum}"
}


resource "kubernetes_config_map" "haystack-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "haystack-ui.json" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
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
    ui_enable_sso = "${var.ui_enable_sso}"
    ui_saml_callback_url = "${var.ui_saml_callback_url}"
    ui_saml_entry_point = "${var.ui_saml_entry_point}"
    ui_saml_issuer = "${var.ui_saml_issuer}"
    ui_session_secret = "${var.ui_session_secret}"
    metricpoint_encoder_type = "${var.metricpoint_encoder_type}"
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
    memory_request = "${var.memory_request}"
    cpu_limit = "${var.cpu_limit}"
    cpu_request = "${var.cpu_request}"
    service_port = "${var.service_port}"
    container_port = "${var.container_port}"
    configmap_name = "${local.configmap_name}"
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
