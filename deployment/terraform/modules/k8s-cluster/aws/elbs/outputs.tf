output "api-elb-id" {
  value = "${aws_elb.k8s-api-elb.id}"
}
output "api-elb-dns_name" {
  value = "${aws_route53_record.k8s-api-elb-route53.name}"
}

output "nodes-elb-id" {
  value = "${aws_elb.k8s-nodes-elb.id}"
}

output "nodes-elb-dns_name" {
  value = "${aws_route53_record.k8s-nodes-elb-route53.name}"
}