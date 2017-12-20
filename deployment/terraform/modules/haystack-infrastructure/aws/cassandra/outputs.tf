output "haystack-cassandra-cname" {
  value = "${aws_route53_record.haystack-cassandra-cname.fqdn}"
}
output "cassandra_hostname" {
  value = "${aws_route53_record.haystack-cassandra-cname.fqdn}"
}
output "cassandra_port" {
  value = "9042"
}