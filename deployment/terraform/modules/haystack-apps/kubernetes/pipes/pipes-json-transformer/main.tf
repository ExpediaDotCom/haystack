locals {
  app_name = "pipes-json-transformer"
  count = "${var.enabled?1:0}"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
}


data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    kafka_hostname = "${var.kafka_hostname}"
    node_selecter_label = "${var.node_selecter_label}"
    replicas = "${var.replicas}"
    image = "${var.image}"
    memory_limit = "${var.memory_limit}"
    memory_request = "${var.memory_request}"
    jvm_memory_limit = "${var.jvm_memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    cpu_request = "${var.cpu_request}"
    graphite_host = "${var.graphite_hostname}"
    graphite_port = "${var.graphite_port}"
    env_vars= "${indent(9,"${var.env_vars}")}"
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
