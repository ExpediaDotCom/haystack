locals {
  "k8s_app_namespace" = "haystack-apps"
}
data "template_file" "traefik_cluster_addon_config" {
  template = "${file("${path.module}/templates/traefik-yaml.tpl")}"
  vars {
    traefik_image = "${var.k8s_traefik_image}"
    haystack_ui_cname = "${var.haystack_ui_cname}"
    traefik_name = "${var.traefik_name}",
    node_port = "${var.traefik_node_port}",
    k8s_app_namespace = "${local.k8s_app_namespace}"
    traefik_replicas = "${var.traefik_replicas}"
  }
}

resource "null_resource" "haystack_app_namespace" {
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create namespace ${local.k8s_app_namespace} --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete namespace ${local.k8s_app_namespace} --context ${var.kubectl_context_name}"
    when = "destroy"
  }
}

resource "null_resource" "traefik_cluster_addon" {
  triggers {
    template = "${data.template_file.traefik_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.traefik_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.traefik_cluster_addon_config.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  depends_on = [
    "null_resource.haystack_app_namespace"]
}
