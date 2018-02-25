locals {
  app_name = "trace-reader"
  config_file_path = "${path.module}/templates/trace-reader_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count = "${var.enabled?1:0}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"

  vars {
    elasticsearch_endpoint = "${var.elasticsearch_endpoint}"
    cassandra_hostname = "${var.cassandra_hostname}"
  }
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"
  vars {
    app_name = "${local.app_name}"
    namespace = "${var.namespace}"
    config = "${replace("${data.template_file.config_data.rendered}","\"","\\\"")}"
    graphite_port = "${var.graphite_port}"
    graphite_host = "${var.graphite_hostname}"
    node_selecter_label = "${var.node_selecter_label}"
    image = "${var.image}"
    replicas = "${var.replicas}"
    memory_limit = "${var.memory_limit}"
    cpu_limit = "${var.cpu_limit}"
    service_port = "${var.service_port}"
    container_port = "${var.container_port}"

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
