output "cassandra_hostname" {
  value = "${aws_route53_record.haystack-cassandra-cname.fqdn}"
  depends_on = ["aws_route53_record.haystack-cassandra-cname"]
}
output "cassandra_port" {
  value = "9042"
}