locals {
  app_name = "pipes-firehose-writer"
  count = "${var.enabled?1:0}"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
}


data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    cpu_limit = "${var.cpu_limit}"
    env_vars= "${indent(9,"${var.env_vars}")}"
    firehose_initialretrysleep = "${var.firehose_initialretrysleep}"
    firehose_kafka_threadcount = "${var.firehose_kafka_threadcount}"
    firehose_maxbatchinterval = "${var.firehose_maxbatchinterval}"
    firehose_maxretrysleep = "${var.firehose_maxretrysleep}"
    firehose_region = "${var.firehose_signingregion}"
    firehose_stream_name = "${var.firehose_streamname}"
    firehose_url = "${var.firehose_url}"
    firehose_usestringbuffering = "${var.firehose_usestringbuffering}"
    firehose_writer_haystack_kafka_fromtopic = "${var.firehose_writer_haystack_kafka_fromtopic}"
    graphite_host = "${var.graphite_hostname}"
    graphite_port = "${var.graphite_port}"
    image = "${var.image}"
    jvm_memory_limit = "${var.jvm_memory_limit}"
    kafka_hostname = "${var.kafka_hostname}"
    memory_limit = "${var.memory_limit}"
    namespace = "${var.namespace}"
    node_selecter_label = "${var.node_selecter_label}"
    replicas = "${var.replicas}"
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