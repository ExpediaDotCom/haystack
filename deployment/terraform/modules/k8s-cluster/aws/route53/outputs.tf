output "haystack_ui_cname" {
  value = "${aws_route53_record.haystack-ui-route53.name}"
}
