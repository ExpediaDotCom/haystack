locals {
  count = "${var.enabled && (var.logging_backend == "" || var.logging_backend == "es") ? 1 : 0 }"
}

data "template_file" "curator_cron_job" {
  template = "${file("${path.module}/templates/curator-cron-job-yaml.tpl")}"
  vars {
    elasticsearch_host = "${var.elasticsearch_host}"
    node_selecter_label = "${var.monitoring-node_selecter_label}"
    curator_image = "${var.curator_image}"
  }
}

resource "null_resource" "elasticsearch_addons" {
  triggers {
    template = "${data.template_file.curator_cron_job.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.curator_cron_job.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.curator_cron_job.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name} || true"
    when = "destroy"
  }
  count = "${local.count}"
}


