output "api-elb-id" {
  value = "${aws_elb.api-elb.id}"
}
output "api-elb-dns_name" {
  value = "${aws_route53_record.api-elb-route53.name}"
}

output "nodes-elb-id" {
  value = "${aws_elb.nodes-elb.id}"
}

output "nodes-elb-dns_name" {
  value = "${aws_route53_record.nodes-elb-route53.name}"
}