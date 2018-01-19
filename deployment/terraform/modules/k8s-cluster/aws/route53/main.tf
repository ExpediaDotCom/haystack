locals {
  elasticsearch-name = "elasticsearch-logging"
  elasticsearch-port = 9200
  k8s_dashboard_cname_count = "${var.k8s_dashboard_cname_enabled?1:0}"
  logs_cname_count = "${var.logs_cname_enabled?1:0}"
  metrics_cname_count = "${var.metrics_cname_enabled?1:0}"
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

resource "aws_route53_record" "haystack-ui-route53" {
  name = "${var.haystack_ui_cname}"
  type = "CNAME"
  records = [
    "${var.nodes_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  depends_on = [
    "aws_route53_record.api-elb-route53"]

}

resource "aws_route53_record" "logs-route53" {
  name = "${var.logs_cname}"
  type = "CNAME"
  records = [
    "${var.nodes_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  depends_on = [
    "aws_route53_record.api-elb-route53"]
  count = "${local.logs_cname_count}"
}

resource "aws_route53_record" "metrics-route53" {
  name = "${var.metrics_cname}"
  type = "CNAME"
  records = [
    "${var.nodes_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  depends_on = [
    "aws_route53_record.api-elb-route53"]
  count = "${local.metrics_cname_count}"
}


resource "aws_route53_record" "k8s-dashboard-route53" {
  name = "${var.k8s_dashboard_cname}"
  type = "CNAME"
  records = [
    "${var.nodes_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  depends_on = [
    "aws_route53_record.api-elb-route53"]
  count = "${local.k8s_dashboard_cname_count}"
}
