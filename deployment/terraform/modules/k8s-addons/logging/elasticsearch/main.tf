locals {
  rendered_elasticsearch_addon_path = "${path.module}/manifests/elasticsearch-addon.yaml"
  elasticsearch-name = "elasticsearch-logging"
  elasticsearch-port = 9200
  count = "${var.enabled?1:0}"
}


data "template_file" "elasticsearch_addon_config" {
  template = "${file("${path.module}/templates/es-logging-yaml.tpl")}"
  vars {
    elasticsearch-name = "${local.elasticsearch-name}"
    minimum_masters = "${var.minimum_masters}"
  }
  count = "${local.count}"
}
resource "null_resource" "elasticsearch_addons" {
  triggers {
    template = "${data.template_file.elasticsearch_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_elasticsearch_addon_path} <<EOL\n${data.template_file.elasticsearch_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_elasticsearch_addon_path}"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_elasticsearch_addon_path}"
    when = "destroy"
  }
  count = "${local.count}"
}
