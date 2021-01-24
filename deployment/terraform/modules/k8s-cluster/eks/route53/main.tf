resource "aws_route53_record" "cluster-root-route53" {
  name = "${var.cluster_root_cname}"
  type = "CNAME"
  records = [
    "${var.nodes_elb_dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
}

resource "aws_route53_record" "cluster-app-route53" {
  name = "*.${var.cluster_root_cname}"
  type = "CNAME"
  records = [
    "${var.cluster_root_cname}"]
  ttl = 300
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
}
