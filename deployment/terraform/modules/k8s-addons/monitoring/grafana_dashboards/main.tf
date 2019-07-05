locals {
  count = "${var.enabled?1:0}"
}

resource "null_resource" "k8s_grafana_addons" {
  provisioner "local-exec" {
    command = "echo '${file("${path.module}/templates/grafana-dashboards.yaml")}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${file("${path.module}/templates/grafana-dashboards.yaml")}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }

  count = "${local.count}"
}

