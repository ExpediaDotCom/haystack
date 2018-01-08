locals {
  rendered_kibana_addon_path = "${path.module}/manifests/kibana-addon.yaml"
  count = "${var.enabled?1:0}"

}


data "template_file" "kibana_addon_config" {
  template = "${file("${path.module}/templates/kibana-yaml.tpl")}"
  vars {
    elasticsearch_http_endpoint = "${var.elasticsearch_http_endpoint}"
  }
  count = "${local.count}"

}
resource "null_resource" "kibana_addons" {
  triggers {
    template = "${data.template_file.kibana_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_kibana_addon_path} <<EOL\n${data.template_file.kibana_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_kibana_addon_path} --context ${var.k8s_cluster_name}"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_kibana_addon_path} --context ${var.k8s_cluster_name}"
    when = "destroy"
  }
  count = "${local.count}"
}
