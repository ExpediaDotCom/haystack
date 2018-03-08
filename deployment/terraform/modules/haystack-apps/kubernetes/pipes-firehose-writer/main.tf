locals {
  app_name = "pipes-firehose-writer"
  count = "${var.enabled?1:0}"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"

}


data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    firehose_region = "${var.firehose_retrycount}"
    firehose_region = "${var.firehose_signingregion}"
    firehose_stream_name = "${var.firehose_streamname}"
    firehose_url = "${var.firehose_url}"
    kafka_hostname = "${var.kafka_hostname}"
    node_selecter_label = "${var.node_selecter_label}"
    replicas = "${var.replicas}"
    image = "${var.image}"
    memory_limit = "${var.memory_limit}"
    cpu_limit = "${var.cpu_limit}"
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