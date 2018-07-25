locals {
  app_name = "kafka-service"
  service_port = 9092
  container_port = 9092
  image = "wurstmeister/kafka:0.11.0.1"
  topics = "proto-spans:1:1,metricpoints:1:1,mdm:1:1,span-buffer:1:1,graph-nodes:1:1,ewma-metrics:1:1,constant-metrics:1:1,pewma-metrics:1:1,anomalies:1:1,aa-metrics:1:1,mapped-metrics:1:1"
  deployment_yaml_file_path = "${path.module}/templates/deployment-yaml.tpl"
}

module "zookeeper" {
  source = "zookeeper"
  replicas = "1"
  namespace = "${var.namespace}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  node_selecter_label = "${var.node_selecter_label}"
  memory_limit = "${var.memory_limit}"
  jvm_memory_limit = "${var.jvm_memory_limit}"
  cpu_limit = "${var.cpu_limit}"
}


data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    node_selecter_label = "${var.node_selecter_label}"
    replicas = "${var.replicas}"
    image = "${local.image}"
    memory_limit = "${var.memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    jvm_memory_limit = "${var.jvm_memory_limit}"
    service_port = "${local.service_port}"
    container_port = "${local.container_port}"
    host_name = "${var.docker_host_ip}"
    zk_endpoint = "${module.zookeeper.zookeeper_service_name}:${module.zookeeper.zookeeper_service_port}"
    topics = "${local.topics}"
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
