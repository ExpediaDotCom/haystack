locals {
  count = "${var.enabled?1:0}"
}


data "template_file" "whitelisted-fields-pod-yaml" {
  template = "${file("${path.module}/templates/whitelisted-fields-pod-yaml.tpl")}"
  vars {
    elasticsearch_host = "${var.elasticsearch_hostname}"
    elasticsearch_port = "${var.elasticsearch_port}"
    app_namespace = "${var.namespace}"
  }
}
resource "null_resource" "whitelisted-fields-pod" {
  triggers {
    template = "${data.template_file.whitelisted-fields-pod-yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.whitelisted-fields-pod-yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.whitelisted-fields-pod-yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
}


