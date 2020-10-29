locals {
  count = "${var.enabled?1:0}"
}

data "template_file" "grafana_dashboard_addon_config" {
  template = "${file("${path.module}/templates/grafana-dashboards.yaml")}"
  vars {
    haystack_grafana_dashboard_image = "${var.haystack_grafana_dashboard_image}"
  }
}

resource "null_resource" "k8s_grafana_addons" {
  triggers {
    template = "${data.template_file.grafana_dashboard_addon_config.rendered}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.grafana_dashboard_addon_config.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.grafana_dashboard_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }

  count = "${local.count}"
}

