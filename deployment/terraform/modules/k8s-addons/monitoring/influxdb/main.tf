locals {
  count = "${var.enabled?1:0}"

}
//creating the influxdb cluster addon for pushing k8s logs to elastic search

data "template_file" "influxdb_cluster_addon_config" {
  template = "${file("${path.module}/templates/influxdb-yaml.tpl")}"
  vars {
    influxdb_image = "${var.k8s_influxdb_image}"
    influxdb_storage_class = "${var.storage_class}"
    influxdb_storage = "${var.storage_volume}"
  }
  count = "${local.count}"
}

resource "null_resource" "k8s_influxdb_addons" {
  triggers {
    template = "${data.template_file.influxdb_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.influxdb_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.k8s_cluster_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.influxdb_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.k8s_cluster_name}"
    when = "destroy"
  }

  count = "${local.count}"
}