locals {
  count = "${var.enabled?1:0}"
}


data "template_file" "grafana_cluster_addon_config" {
  template = "${file("${path.module}/templates/grafana-yaml.tpl")}"
  vars {
    grafana_image = "${var.k8s_grafana_image}"
    grafana_storage_class = "${var.k8s_grafana_storage_class}"
    grafana_storage = "${var.k8s_grafana_storage}"
    grafana_root_path = "${var.k8s_grafana_root_path}"

  }
  count = "${local.count}"

}

resource "null_resource" "k8s_grafana_addons" {
  triggers {
    template = "${data.template_file.grafana_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.grafana_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.k8s_cluster_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.grafana_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.k8s_cluster_name}"
    when = "destroy"
  }
  count = "${local.count}"

}

