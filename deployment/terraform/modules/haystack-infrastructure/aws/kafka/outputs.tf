output "kafka_service_name" {
  value = "${aws_route53_record.haystack-kafka-cname.fqdn}"
}

output "kafka_port" {
  value = "${local.kafka_port}"
}