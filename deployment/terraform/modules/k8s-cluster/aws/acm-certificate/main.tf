resource "aws_acm_certificate" "cert" {
  domain_name               = "*.${var.cluster["domain_name"]}"
  subject_alternative_names = [ "*.${var.cluster["name"]}.${var.cluster["domain_name"]}" ]
  validation_method         = "DNS"

  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-acm-certificate",
    "Name", "${var.cluster["name"]}-acm-certificate",
    "Component", "K8s"
  ))}"
}

resource "aws_route53_record" "cert_validation" {
  name    = "${aws_acm_certificate.cert.domain_validation_options.0.resource_record_name}"
  type    = "${aws_acm_certificate.cert.domain_validation_options.0.resource_record_type}"
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  records = ["${aws_acm_certificate.cert.domain_validation_options.0.resource_record_value}"]
  ttl     = 60
  allow_overwrite = true
}

resource "aws_route53_record" "cert_validation_alt1" {
  name    = "${aws_acm_certificate.cert.domain_validation_options.1.resource_record_name}"
  type    = "${aws_acm_certificate.cert.domain_validation_options.1.resource_record_type}"
  zone_id = "/hostedzone/${var.aws_hosted_zone_id}"
  records = ["${aws_acm_certificate.cert.domain_validation_options.1.resource_record_value}"]
  ttl     = 60
  allow_overwrite = true
}

resource "aws_acm_certificate_validation" "cert" {
  certificate_arn = "${aws_acm_certificate.cert.arn}"

  validation_record_fqdns = [
    "${aws_route53_record.cert_validation.fqdn}",
    "${aws_route53_record.cert_validation_alt1.fqdn}"
  ]
}
