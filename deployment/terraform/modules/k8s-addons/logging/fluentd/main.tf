locals {
  count = "${var.enabled && (var.logging_backend == "" || var.logging_backend == "es") ? 1 : 0 }"
}

//creating the fluentd cluster addon for pushing k8s logs to elastic search

data "template_file" "fluentd_cluster_addon_config" {
  template = "${file("${path.module}/templates/fluentd-es-yaml.tpl")}"
  vars {
    elasticsearch_host = "${var.elasticsearch_host}"
    elasticsearch_port = "${var.elasticsearch_port}"
    fluentd_image = "${var.k8s_fluentd_image}"
    container_log_path = "${var.container_log_path}"
  }
}

resource "null_resource" "k8s_fluentd_addons" {
  triggers {
    template = "${data.template_file.fluentd_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.fluentd_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.kubectl_context_name}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.fluentd_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }
  count = "${local.count}"
}
