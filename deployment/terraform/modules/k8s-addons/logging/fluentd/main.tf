locals {
  rendered_fluent_d_addon_path = "${path.module}/manifests/fluentd-cluster-addon.yaml"
  count = "${var.enabled?1:0}"
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
  count = "${local.count}"
}

resource "null_resource" "k8s_fluentd_addons" {
  triggers {
    template = "${data.template_file.fluentd_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_fluent_d_addon_path} <<EOL\n${data.template_file.fluentd_cluster_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_fluent_d_addon_path} --context ${var.k8s_cluster_name} "
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_fluent_d_addon_path} --context ${var.k8s_cluster_name} "
    when = "destroy"
  }
  count = "${local.count}"
}
