output "haystack_ui_cname" {
  value = "${aws_route53_record.haystack-ui-route53.0.name}"
}
output "k8s_cluster_name" {
  value = "${aws_route53_record.k8s-dashboard-route53.0.name}"
}