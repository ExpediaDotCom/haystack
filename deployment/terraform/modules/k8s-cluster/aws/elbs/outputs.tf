output "master-elb-dns_name" {
  value = "${aws_elb.api-elb.dns_name}"
}


output "app-nodes-elb-dns_name" {
  value = "${aws_elb.nodes-elb.dns_name}"
}

output "monitoring-nodes-elb-dns_name" {
  value = "${aws_elb.monitoring-elb.dns_name}"
}
output "app-nodes-nlb-arn" {
  value = "${aws_lb.nodes-nlb-endpoint-service.*.arn}"
}