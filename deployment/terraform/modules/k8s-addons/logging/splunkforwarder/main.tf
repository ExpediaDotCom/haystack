locals {
  count = "${var.enabled && var.logging_backend == "splunk" ? 1 : 0 }"
}

data "template_file" "splunkforwarder_config" {
  template = "${file("${path.module}/templates/splunkforwarder-yaml.tpl")}"
  vars {
    node_selecter_label = "${var.monitoring-node_selecter_label}"
    splunk_deployment_server = "${var.splunk_deployment_server}"
    cluster_name = "${var.cluster_name}"
    splunk_index = "${var.splunk_index}"
    splunk_forwarder_image = "${var.splunk_forwarder_image}"
  }
}

resource "null_resource" "splunkforwarder_addon" {
  triggers {
    template = "${data.template_file.splunkforwarder_config.rendered}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.splunkforwarder_config.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.splunkforwarder_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }

  count = "${local.count}"
}
