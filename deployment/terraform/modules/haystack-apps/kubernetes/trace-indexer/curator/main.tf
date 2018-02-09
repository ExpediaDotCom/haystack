locals {
  count = "${var.enabled?1:0}"
}


data "template_file" "curator_cron_job" {
  template = "${file("${path.module}/templates/curator-cron-job-yaml.tpl")}"
  vars {
    elasticsearch_host = "${var.elasticsearch_hostname}"
    node_selecter_label = "${var.monitoring-node_selecter_label}"
  }
  count = "${local.count}"
}
resource "null_resource" "curator_addons" {
  triggers {
    template = "${data.template_file.curator_cron_job.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.curator_cron_job.rendered}' | ${var.kubectl_executable_name} create -f - --context ${var.kubectl_context_name}"
  }

  provisioner "local-exec" {
    command = "echo '${data.template_file.curator_cron_job.rendered}' | ${var.kubectl_executable_name} delete -f - --context ${var.kubectl_context_name}"
    when = "destroy"
  }
  count = "${local.count}"
}


