locals {
  app_name                  = "aquila-trainer"
  configmap_name            = "${local.app_name}-${local.checksum}"
  checksum                  = "${sha1("${data.template_file.config_data.rendered}")}"
  config_file_path          = "${path.module}/templates/application_conf.tpl"
  deployment_yaml_file_path = "${path.module}/templates/deployment_yaml.tpl"
  count                     = "${var.enabled ? 1 : 0}"
}

data "template_file" "config_data" {
  template = "${file("${local.config_file_path}")}"
}

data "template_file" "deployment_yaml" {
  template = "${file("${local.deployment_yaml_file_path}")}"

  vars {
    app_name            = "${local.app_name}"

    # Docker
    image               = "${var.image}"
    image_pull_policy   = "${var.image_pull_policy}"

    # Kubernetes
    namespace           = "${var.namespace}"
    replicas            = "${var.replicas}"
    cpu_limit           = "${var.cpu_limit}"
    cpu_request         = "${var.cpu_request}"
    memory_limit        = "${var.memory_limit}"
    memory_request      = "${var.memory_request}"
    node_selector_label = "${var.node_selector_label}"
    configmap_name      = "${local.configmap_name}"
    service_port        = "${var.service_port}"
    container_port      = "${var.container_port}"

    # Environment
    jvm_memory_limit    = "${var.jvm_memory_limit}"
    graphite_enabled    = "${var.graphite_enabled}"
    graphite_host       = "${var.graphite_hostname}"
    graphite_port       = "${var.graphite_port}"
    env_vars            = "${indent(9, "${var.env_vars}")}"
  }
}

resource "kubernetes_config_map" "haystack-config" {
  metadata {
    name      = "${local.configmap_name}"
    namespace = "${var.namespace}"
  }
  data {
    # Looks like we can't use ${local.app_name} here. [WLW]
    "aquila-trainer.conf" = "${data.template_file.config_data.rendered}"
  }
  count = "${local.count}"
}

# Deployment via kubectl since the kubernetes provider doesn't natively support deployment.

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
    when    = "destroy"
  }
  count = "${local.count}"
}
