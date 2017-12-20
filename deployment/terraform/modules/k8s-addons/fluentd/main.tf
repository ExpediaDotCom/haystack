locals {
  rendered_fluent_d_addon_path = "${path.module}/manifests/fluentd-cluster-addon.yaml"
  rendered_elasticsearch_addon_path = "${path.module}/manifests/elasticsearch-addon.yaml"
  elasticsearch-name = "elasticsearch-logging"
}


data "template_file" "elasticsearch_addon_config" {
  template = "${file("${path.module}/templates/es-logging-yaml.tpl")}"
  vars {
    elasticsearch-name = "${local.elasticsearch-name}"
  }
}
resource "null_resource" "elasticsearch_addons" {
  triggers {
    template = "${data.template_file.elasticsearch_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_elasticsearch_addon_path} <<EOL\n${data.template_file.elasticsearch_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_elasticsearch_addon_path}"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_elasticsearch_addon_path}"
    when = "destroy"
  }
}

//creating the fluentd cluster addon for pushing k8s logs to elastic search

data "template_file" "fluentd_cluster_addon_config" {
  template = "${file("${path.module}/templates/fluentd-es-yaml.tpl")}"
  vars {
    aws_elastic_search_url = "http://${local.elasticsearch-name}:9200"
    fluentd_image = "${var.k8s_fluentd_image}"
  }
  depends_on = [
    "null_resource.elasticsearch_addons"]
}

resource "null_resource" "k8s_fluentd_addons" {
  triggers {
    template = "${data.template_file.fluentd_cluster_addon_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_fluent_d_addon_path} <<EOL\n${data.template_file.fluentd_cluster_addon_config.rendered}EOL"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} create -f ${local.rendered_fluent_d_addon_path}"
  }
  provisioner "local-exec" {
    command = "${var.kubectl_executable_name} delete -f ${local.rendered_fluent_d_addon_path}"
    when = "destroy"
  }
}
