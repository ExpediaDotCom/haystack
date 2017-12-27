locals {
  rendered_fluent_d_addon_path = "${path.module}/manifests/fluentd-cluster-addon.yaml"
  count = "${var.enabled?1:0}"
}

//creating the fluentd cluster addon for pushing k8s logs to elastic search

data "template_file" "fluentd_cluster_addon_config" {
  template = "${file("${path.module}/templates/fluentd-es-yaml.tpl")}"
  vars {
    aws_elastic_search_url = "${var.elasticsearch_http_endpoint}"
    fluentd_image = "${var.k8s_fluentd_image}"
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
    command = "${var.kubectl_executable_name} create -f ${local.rendered_fluent_d_addon_path}"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_fluent_d_addon_path}"
    when = "destroy"
  }
  count = "${local.count}"
}
