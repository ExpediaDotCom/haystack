locals {
  app_name = "metrictank"
  service_port = 6060
  container_port = 6060
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  image = "grafana/metrictank:0.9.0"
  count = "${var.enabled == "true" ? 1:0}"

}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    gra = "${var.graphite_address}"
    graphite_address = "${var.graphite_address}"
    node_selecter_label = "${var.node_selecter_label}"
    kafka_address = "${var.kafka_address}"
    cassandra_address = "${var.cassandra_address}"
    replicas = "${var.replicas}"
    image = "${local.image}"
    memory_limit = "${var.memory_limit}"
    memory_request = "${var.memory_request}"
    cpu_limit = "${var.cpu_limit}"
    cpu_request = "${var.cpu_request}"
    service_port = "${local.service_port}"
    container_port = "${local.container_port}"
    env_vars= "${indent(9,"${var.env_vars}")}"
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
  count = "${local.count}"
}
