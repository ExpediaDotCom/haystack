output "app-nodes-elb-dns_name" {
  value = "${aws_elb.nodes-elb.dns_name}"
}
