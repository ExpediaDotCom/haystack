output "master-elb-id" {
  value = "${aws_elb.api-elb.id}"
}

output "master-elb-dns_name" {
  value = "${aws_elb.api-elb.dns_name}"
}

output "nodes-elb-id" {
  value = "${aws_elb.nodes-elb.id}"
}

output "nodes-elb-dns_name" {
  value = "${aws_elb.nodes-elb.dns_name}"
}

output "monitoring-elb-dns_name" {
  value = "${aws_elb.monitoring-elb.dns_name}"
}