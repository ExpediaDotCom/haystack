output "haystack_ui_cname" {
  value = "${aws_route53_record.root-route53.0.name}"
}

output "cluster_name" {
  value = "${data.null_data_source.dependency.outputs["cluster_name"]}"

}

output "graphite_cname" {
  value = "${aws_route53_record.monitoring-elb-route53.name}"
}