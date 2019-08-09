locals {
  app_name = "blobs-http-reverse-proxy"
  deployment_yaml_file_path = "${path.module}/templates/deployment.yaml"
  count = "${var.reverse-proxy["enabled"]?1:0}"
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"

  vars {
    app_name = "${local.app_name}"
    node_selecter_label = "${var.node_selector_label}"
    image = "expediadotcom/blobs-http-reverse-proxy:${var.reverse-proxy["version"]}"
    replicas = "${var.reverse-proxy["proxy_instances"]}"
    enabled = "${var.reverse-proxy["enabled"]}"
    cpu_limit = "${var.reverse-proxy["proxy_cpu_limit"]}"
    cpu_request = "${var.reverse-proxy["proxy_cpu_request"]}"
    memory_limit = "${var.reverse-proxy["proxy_memory_limit"]}"
    memory_request = "${var.reverse-proxy["proxy_memory_request"]}"
    jvm_memory_limit = "${var.reverse-proxy["proxy_jvm_memory_limit"]}"
    kubectl_context_name = "${var.kubectl_context_name}"
    kubectl_executable_name = "${var.kubectl_executable_name}"
    namespace = "${var.namespace}"
    grpc_server_endpoint = "${var.reverse-proxy["grpc_server_endpoint"]}"
    service_port = "${var.service_port}"
  }
}

resource "null_resource" "reverseproxy_addon" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }

  count = "${local.count}"
}
