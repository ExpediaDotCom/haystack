output "haystack-cassandra-cname" {
  value = "${aws_route53_record.haystack-cassandra-cname.fqdn}"
}
