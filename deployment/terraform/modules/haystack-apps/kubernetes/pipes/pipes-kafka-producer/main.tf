locals {
  app_name = "pipes-kafka-producer"
  count = "${var.enabled?1:0}"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"

}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    kafka_hostname = "${var.kafka_hostname}"
    node_selecter_label = "${var.node_selecter_label}"
    jvm_memory_limit = "${var.jvm_memory_limit}"
    replicas = "${var.replicas}"
    image = "${var.image}"
    memory_limit = "${var.memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    graphite_host = "${var.graphite_hostname}"
    graphite_port = "${var.graphite_port}"
    env_vars= "${indent(9,"${var.env_vars}")}"
  }
}