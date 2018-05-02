locals {
  app_name = "pipes-secret-detector"
  count = "${var.enabled?1:0}"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
}


data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    cpu_limit = "${var.cpu_limit}"
    env_vars= "${indent(9,"${var.env_vars}")}"
    graphite_host = "${var.graphite_hostname}"
    graphite_port = "${var.graphite_port}"
    image = "${var.image}"
    kafka_hostname = "${var.kafka_hostname}"
    memory_limit = "${var.memory_limit}"
    namespace = "${var.namespace}"
    node_selecter_label = "${var.node_selecter_label}"
    pipes_secret_detector_secretsnotifications_email_from = "${var.pipes_secret_detector_secretsnotifications_email_from}"
    pipes_secret_detector_secretsnotifications_email_host = "${var.pipes_secret_detector_secretsnotifications_email_host}"
    pipes_secret_detector_secretsnotifications_email_subject = "${var.pipes_secret_detector_secretsnotifications_email_subject}"
    pipes_secret_detector_secretsnotifications_email_tos = "${var.pipes_secret_detector_secretsnotifications_email_tos}"
    pipes_secret_detector_secretsnotifications_whitelist_bucket = "${var.pipes_secret_detector_secretsnotifications_whitelist_bucket}"
    pipes_secret_detector_kafka_threadcount = "${var.pipes_secret_detector_kafka_threadcount}"
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