locals {
  elasticsearch-name = "elasticsearch-logging"
  elasticsearch-port = 9200
  count = "${var.enabled?1:0}"
}


data "template_file" "elasticsearch_addon_config" {
  template = "${file("${path.module}/templates/es-logging-yaml.tpl")}"
  vars {
    elasticsearch-name = "${local.elasticsearch-name}"
    minimum_masters = "${var.minimum_masters}"
    storage_class = "${var.storage_class}"
    storage_volume = "${var.storage_volume}"
    node_selecter_label = "${var.monitoring-node_selecter_label}"
  }
  count = "${local.count}"
}
resource "null_resource" "elasticsearch_addons" {
  triggers {
    template = "${data.template_file.elasticsearch_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.elasticsearch_addon_config.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.elasticsearch_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
}


