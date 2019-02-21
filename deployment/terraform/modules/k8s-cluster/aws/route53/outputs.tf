output "haystack_ui_cname" {
  value = "${aws_route53_record.root-route53.0.name}"
}