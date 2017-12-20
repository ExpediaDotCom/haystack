locals {
  rendered_grafana_addon_path = "${path.module}/manifests/grafana-cluster-addon.yaml"
}


data "template_file" "grafana_cluster_addon_config" {
  template = "${file("${path.module}/templates/grafana-yaml.tpl")}"
  vars {
    grafana_image = "${var.k8s_grafana_image}"
    grafana_storage_class = "${var.k8s_grafana_storage_class}"
    grafana_storage = "${var.k8s_grafana_storage}"
    grafana_root_path = "{var.k8s_grafana_root_path}"

  }
}

resource "null_resource" "k8s_grafana_addons" {
  triggers {
    template = "${data.template_file.grafana_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_grafana_addon_path} <<EOL\n${data.template_file.grafana_cluster_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_grafana_addon_path}"
  }

  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_grafana_addon_path}"
    when = "destroy"
  }
}

