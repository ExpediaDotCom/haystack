locals {
  rendered_influxdb_addon_path = "${path.module}/manifests/influxdb-cluster-addon.yaml"
}


//creating the influxdb cluster addon for pushing k8s logs to elastic search

data "template_file" "influxdb_cluster_addon_config" {
  template = "${file("${path.module}/templates/influxdb-yaml.tpl")}"
  vars {
    influxdb_image = "${var.k8s_influxdb_image}"
    influxdb_storage_class = "${var.k8s_influxdb_storage_class}"
    influxdb_storage = "${var.k8s_influxdb_storage}"

  }
}

resource "null_resource" "k8s_influxdb_addons" {
  triggers {
    template = "${data.template_file.influxdb_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_influxdb_addon_path} <<EOL\n${data.template_file.influxdb_cluster_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_influxdb_addon_path}"
  }

  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_influxdb_addon_path}"
    when = "destroy"
  }
}