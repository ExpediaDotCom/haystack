locals {
  count = "${var.enabled?1:0}"
}

data "template_file" "dashboard_cluster_addon_config" {
  template = "${file("${path.module}/templates/dashboard-yaml.tpl")}"
  vars {
    dashboard_image = "${var.k8s_dashboard_image}"
    dashboard_cname = "${var.k8s_dashboard_cname}"
    node_selecter_label = "${var.monitoring-node_selecter_label}"
  }
  count = "${local.count}"
}

resource "null_resource" "k8s_dashboard_addons" {
  triggers {
    template = "${data.template_file.dashboard_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.dashboard_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.dashboard_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }
  count = "${local.count}"
}
