locals {
  rendered_traefik_addon_path = "${path.module}/manifests/traefik-addon.yaml"
}
//creating the traefik cluster addon


data "template_file" "traefik_cluster_addon_config" {
  template = "${file("${path.module}/templates/traefik-yaml.tpl")}"
  vars {
    traefik_image = "${var.k8s_traefik_image}"
    haytack_domain_name = "${var.haystack_domain_name}"
    traefik_name = "${var.traefik_name}",
    node_port = "${var.traefik_node_port}",
    k8s_app_namespace = "${var.k8s_app_namespace}"
    traefik_replicas = "${var.traefik_replicas}"
  }
}

resource "null_resource" "traefik_cluster_addon" {
  triggers {
    template = "${data.template_file.traefik_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_traefik_addon_path} <<EOL\n${data.template_file.traefik_cluster_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_traefik_addon_path}"
  }

  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_traefik_addon_path}"
    when = "destroy"
  }

}