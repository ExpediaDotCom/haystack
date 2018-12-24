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
    heap_memory_in_mb = "${var.heap_memory_in_mb}"
    graphite_node_port = "${var.graphite_node_port}"
    node_selecter_label = "${var.node_selecter_label}"
  }
}

resource "null_resource" "k8s_influxdb_addons" {
  triggers {
    template = "${data.template_file.influxdb_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.influxdb_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.influxdb_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }

  count = "${local.count}"
}


resource "null_resource" "k8s_influxdb_retention" {
  triggers {
    template = "${data.template_file.influxdb_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${file("${path.module}/manifests/influx_db_retention.yaml")}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${file("${path.module}/manifests/influx_db_retention.yaml")}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }

  count = "${local.count}"
}