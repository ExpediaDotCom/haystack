locals {
  app_name = "trace-indexer"
  config_file_path = "${path.module}/templates/trace-indexer_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count = "${var.enabled?1:0}"
  span_produce_topic = "${var.enable_kafka_sink?"span-buffer":""}"
  elasticsearch_endpoint = "${var.elasticsearch_hostname}:${var.elasticsearch_port}"
  configmap_name = "${local.app_name}-${random_integer.id.id}"
}


resource "random_integer" "id" {
  min = 1
  max = 9999
  keepers = {
    # Generate a new integer each time the configuration changes
    config_change = "${data.template_file.config_data.rendered}"
  }
}

resource "kubernetes_config_map" "haystack-config" {
  metadata {
    name = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    "trace-indexer.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    kafka_endpoint = "${var.kafka_endpoint}"
    elasticsearch_endpoint = "${local.elasticsearch_endpoint}"
    cassandra_hostname = "${var.cassandra_hostname}"
    span_produce_topic = "${local.span_produce_topic}"
  }
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    graphite_port = "${var.graphite_port}"
    graphite_host = "${var.graphite_hostname}"
    node_selecter_label = "${var.node_selecter_label}"
    image = "${var.image}"
    replicas = "${var.replicas}"
    memory_limit = "${var.memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    configmap_name = "${local.configmap_name}"
  }
}

resource "null_resource" "kubectl_apply" {
  triggers {
    template = "${data.template_file.deployment_yaml.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} apply -f - --context ${var.kubectl_context_name}"
  }
  count = "${local.count}"
}


resource "null_resource" "kubectl_destroy" {

  provisioner "local-exec" {
    command = "echo '${data.template_file.deployment_yaml.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
}


module "curator" {
  source = "curator"
  kubectl_context_name = "${var.kubectl_context_name}"
  enabled = "${var.enabled}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  namespace = "${var.namespace}"
}
