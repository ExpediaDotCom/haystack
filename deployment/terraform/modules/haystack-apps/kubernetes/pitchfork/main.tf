locals {
  app_name = "pitchfork"
  deployment_yaml_file_path = "${path.module}/templates/deployment.yaml"
  count = "${var.pitchfork["enabled"]?1:0}"
  
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    pitchfork_image = "${var.pitchfork["image"]}"
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    replicas = "${var.pitchfork["instances"]}"
    cpu_request = "${var.pitchfork["cpu_request"]}"
    cpu_limit = "${var.pitchfork["cpu_limit"]}"
    memory_limit = "${var.pitchfork["memory_limit"]}"
    memory_request = "${var.pitchfork["memory_request"]}"
    jvm_memory_limit = "${var.pitchfork["jvm_memory_limit"]}"
    pitchfork_port = "${var.pitchfork["pitchfork_port"]}"
    kafka_enabled = "${var.pitchfork["kafka_enabled"]}"
    logging_enabled = "${var.pitchfork["logging_enabled"]}"
    logging_span_enabled = "${var.pitchfork["logging_span_enabled"]}"
    kafka_topic = "${var.pitchfork["kafka_topic"]}"
    kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
    env_vars= "${indent(9,var.pitchfork["env_vars"])}"
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
