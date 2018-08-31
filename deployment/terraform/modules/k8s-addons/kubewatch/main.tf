locals {
  count = "${var.enabled?1:0}"
}


data "template_file" "kubewatch_addon_config" {
  template = "${file("${path.module}/templates/kubewatch-deployment-yaml.tpl")}"
  vars {
    kubewatch_image = "${var.kubewatch_image}"
    node_selecter_label = "${var.node_selecter_label}"
    kubewatch_config_yaml_base64 = "${var.kubewatch_config_yaml_base64}"
  }
  count = "${local.count}"
}

resource "null_resource" "k8s_kubewatch_addons" {
  triggers {
    template = "${data.template_file.kubewatch_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.kubewatch_addon_config.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.kubewatch_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }
  count = "${local.count}"
}
