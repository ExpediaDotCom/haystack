locals {
  app_name = "elasticsearch"
  service_port = 9200
  container_port = 9200
  deployment_yaml_file_path = "${path.module}/templates/deployment-yaml.tpl"
  es_docker_image = "elasticsearch:5-alpine"
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    node_selecter_label = "${var.node_selecter_label}"
    replicas = "${var.replicas}"
    image = "${local.es_docker_image}"
    memory_limit = "${var.memory_limit}"
    jvm_memory_limit = "${var.jvm_memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    service_port = "${local.service_port}"
    container_port = "${local.container_port}"
  }
}

resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
}