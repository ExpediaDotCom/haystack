locals {
  count = "${var.enabled?1:0}"
}


data "template_file" "grafana_cluster_addon_config" {
  template = "${file("${path.module}/templates/grafana-yaml.tpl")}"
  vars {
    grafana_image = "${var.k8s_grafana_image}"
    grafana_storage_class = "${var.storage_class}"
    grafana_storage = "${var.storage_volume}"
    metrics_cname = "${var.metrics_cname}"
    node_selecter_label = "${var.monitoring-node_selecter_label}"

  }
  count = "${local.count}"

}

resource "null_resource" "k8s_grafana_addons" {
  triggers {
    template = "${data.template_file.grafana_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.grafana_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.grafana_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"

}

