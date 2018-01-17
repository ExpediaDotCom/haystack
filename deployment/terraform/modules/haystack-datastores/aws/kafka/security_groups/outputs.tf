output "kafka_broker_security_group_ids" {
  value = "${aws_security_group.haystack-kafka.id}"
}
