locals {
  count = "${var.enabled?1:0}"
  deployment_yaml_file_path = "${path.module}/templates/aa_apps_limits_yaml.tpl"
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    cpu_limit = "${var.cpu_limit}"
    memory_limit = "${var.memory_limit}"
  }
}

resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --namespace=${var.aa_app_namespace}"
  }
  count = "${local.count}"
}


resource "null_resource" "kubectl_destroy" {

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --namespace=${var.aa_app_namespace}"
    when = "destroy"
  }
  count = "${local.count}"
}
