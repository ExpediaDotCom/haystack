locals {
  app_name = "${var.app_name}"
  config_file_path = "${path.module}/templates/kinesis-span-collector_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count = "${var.enabled?1:0}"
  checksum = "${sha1("${data.template_file.config_data.rendered}")}"
  configmap_name = "${local.app_name}-${local.checksum}"
}

resource "kubernetes_config_map" "haystack-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "kinesis-span-collector.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    kinesis_stream_region = "${var.kinesis_stream_region}"
    kinesis_stream_name = "${var.kinesis_stream_name}"
    kafka_endpoint = "${var.kafka_endpoint}"
    sts_role_arn = "${var.sts_role_arn}"
    app_group_name = "${var.haystack_cluster_name}-${var.app_name}"
    max_spansize_validation_enabled = "${var.max_spansize_validation_enabled}"
    max_spansize_log_only = "${var.max_spansize_log_only}"
    max_spansize_limit = "${var.max_spansize_limit}"
    message_tag_key = "${var.message_tag_key}"
    message_tag_value = "${var.message_tag_value}"
    max_spansize_skip_tags = "${var.max_spansize_skip_tags}"
    max_spansize_skip_services = "${var.max_spansize_skip_services}"
  }
}


data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    graphite_port = "${var.graphite_port}"
    graphite_host = "${var.graphite_hostname}"
    node_selecter_label = "${var.node_selecter_label}"
    image = "${var.image}"
    replicas = "${var.replicas}"
    memory_limit = "${var.memory_limit}"
    memory_request = "${var.memory_request}"
    jvm_memory_limit = "${var.jvm_memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    cpu_request = "${var.cpu_request}"
    configmap_name = "${local.configmap_name}"
    env_vars = "${indent(9,"${var.env_vars}")}"

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
