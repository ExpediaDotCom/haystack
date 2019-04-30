locals {
  subdomain_cname = "*.${var.haystack_ui_cname}"
}

resource "aws_route53_record" "api-elb-route53" {
  name = "api.${var.k8s_cluster_name}"
  type = "CNAME"
  records = [
    "${var.master_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  //this would ensure that the cluster is up and configured correctly
  provisioner "local-exec" {
    command = "for i in {1..50}; do ${var.kubectl_executable_name} get nodes --context ${var.k8s_cluster_name} -- && break || sleep 15; done"
  }
}

resource "null_resource" "clusterValidator" {
  provisioner "local-exec" {
    command = "bash ${path.module}/scripts/clusterValidator.sh ${var.kubectl_executable_name}"
  }
  depends_on = [
    "aws_route53_record.api-elb-route53"]
}

resource "aws_route53_record" "root-route53" {
  name = "${var.haystack_ui_cname}"
  type = "CNAME"
  records = [
    "${var.nodes_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  depends_on = [
    "aws_route53_record.api-elb-route53"]

}

resource "aws_route53_record" "subdomain-route53" {
  name = "${local.subdomain_cname}"
  type = "CNAME"
  records = [
    "${var.nodes_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  depends_on = [
    "aws_route53_record.api-elb-route53"]
}

data "null_data_source" "dependency" {
  inputs = {
    cluster_name = "${var.k8s_cluster_name}"
  }
  depends_on = [
    "aws_route53_record.api-elb-route53"
  ]
}
